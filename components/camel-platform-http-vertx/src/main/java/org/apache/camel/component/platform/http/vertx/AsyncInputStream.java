/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.platform.http.vertx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.impl.InboundBuffer;
import org.apache.camel.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ReadStream} that can process an {@link InputStream} in an asynchronous way, so that the content can be pumped
 * to the {@link io.vertx.core.streams.WriteStream} of an {@link io.vertx.core.http.HttpServerResponse}.
 */
public class AsyncInputStream implements ReadStream<Buffer> {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncInputStream.class);

    private final Lock lock = new ReentrantLock();
    private final ReadableByteChannel channel;
    private final Vertx vertx;
    private final Context context;
    private final InboundBuffer<Buffer> queue;
    private long readPos;
    private boolean closed;
    private boolean readInProgress;
    private Handler<Buffer> dataHandler;
    private Handler<Void> endHandler;
    private Handler<Throwable> exceptionHandler;

    public AsyncInputStream(Vertx vertx, Context context, InputStream inputStream) {
        this.vertx = vertx;
        this.context = context;
        this.channel = Channels.newChannel(inputStream);
        this.queue = new InboundBuffer<>(context, 0);
        queue.handler(buffer -> {
            if (buffer.length() > 0) {
                handleData(buffer);
            } else {
                handleEnd();
            }
        });
        queue.drainHandler(v -> doRead());
    }

    @Override
    public AsyncInputStream endHandler(Handler<Void> endHandler) {
        lock.lock();
        try {
            checkStreamClosed();
            this.endHandler = endHandler;
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AsyncInputStream exceptionHandler(Handler<Throwable> exceptionHandler) {
        lock.lock();
        try {
            checkStreamClosed();
            this.exceptionHandler = exceptionHandler;
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AsyncInputStream handler(Handler<Buffer> handler) {
        lock.lock();
        try {
            checkStreamClosed();
            this.dataHandler = handler;
            if (this.dataHandler != null && !this.closed) {
                this.doRead();
            } else {
                queue.clear();
            }
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AsyncInputStream pause() {
        lock.lock();
        try {
            checkStreamClosed();
            queue.pause();
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AsyncInputStream resume() {
        lock.lock();
        try {
            checkStreamClosed();
            queue.resume();
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ReadStream<Buffer> fetch(long amount) {
        checkStreamClosed();
        queue.fetch(amount);
        return this;
    }

    public void close(Handler<AsyncResult<Void>> handler) {
        closeInternal(handler);
    }

    private void checkStreamClosed() {
        if (this.closed) {
            throw new IllegalStateException("Stream closed");
        }
    }

    private void checkContext() {
        Context contextToCheck = vertx.getOrCreateContext();
        if (!contextToCheck.equals(context)) {
            throw new IllegalStateException(
                    "AsyncInputStream must only be used in the context that created it, expected: " + this.context
                                            + " actual " + contextToCheck);
        }
    }

    private void closeInternal(Handler<AsyncResult<Void>> handler) {
        lock.lock();
        try {
            closed = true;
            doClose(handler);
        } finally {
            lock.unlock();
        }
    }

    private void doClose(Handler<AsyncResult<Void>> handler) {
        try {
            channel.close();
            if (handler != null) {
                this.vertx.runOnContext(v -> handler.handle(Future.succeededFuture()));
            }
        } catch (IOException e) {
            if (handler != null) {
                this.vertx.runOnContext(v -> handler.handle(Future.failedFuture(e)));
            }
        }
    }

    private void doRead() {
        checkStreamClosed();
        doRead(ByteBuffer.allocate(IOHelper.DEFAULT_BUFFER_SIZE));
    }

    private void doRead(ByteBuffer buffer) {
        lock.lock();
        try {
            if (!readInProgress) {
                readInProgress = true;
                Buffer buff = Buffer.buffer(IOHelper.DEFAULT_BUFFER_SIZE);
                doRead(buff, 0, buffer, readPos, result -> {
                    if (result.succeeded()) {
                        readInProgress = false;
                        Buffer updatedBuffer = result.result();
                        readPos += updatedBuffer.length();
                        if (queue.write(updatedBuffer) && updatedBuffer.length() > 0) {
                            doRead(buffer);
                        }
                    } else {
                        handleException(result.cause());
                    }
                });
            }
        } finally {
            lock.unlock();
        }
    }

    private void doRead(Buffer writeBuff, int offset, ByteBuffer buffer, long position, Handler<AsyncResult<Buffer>> handler) {
        vertx.executeBlocking(() -> channel.read(buffer))
                .onComplete(result -> {
                    if (result.succeeded()) {
                        Integer bytesRead = result.result();
                        if (bytesRead == -1) {
                            // EOF
                            context.runOnContext((v) -> {
                                buffer.flip();
                                writeBuff.setBytes(offset, buffer);
                                buffer.compact();
                                handler.handle(Future.succeededFuture(writeBuff));
                            });
                        } else if (buffer.hasRemaining()) {
                            // Read from the next offset
                            context.runOnContext((v) -> {
                                doRead(writeBuff, offset, buffer, position + bytesRead, handler);
                            });
                        } else {
                            // All data is written
                            context.runOnContext((v) -> {
                                buffer.flip();
                                writeBuff.setBytes(offset, buffer);
                                buffer.compact();
                                handler.handle(Future.succeededFuture(writeBuff));
                            });
                        }
                    } else {
                        context.runOnContext((v) -> handler.handle(Future.failedFuture(result.cause())));
                    }
                });
    }

    private void handleData(Buffer buffer) {
        Handler<Buffer> handler;
        lock.lock();
        try {
            handler = this.dataHandler;
        } finally {
            lock.unlock();
        }
        if (handler != null) {
            checkContext();
            handler.handle(buffer);
        }
    }

    private void handleEnd() {
        Handler<Void> endHandler;
        lock.lock();
        try {
            dataHandler = null;
            endHandler = this.endHandler;
        } finally {
            lock.unlock();
        }
        if (endHandler != null) {
            checkContext();
            endHandler.handle(null);
        }
    }

    private void handleException(Throwable t) {
        if (exceptionHandler != null && t instanceof Exception) {
            exceptionHandler.handle(t);
        } else {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unhandled error while processing stream", t);
            }
        }
    }
}
