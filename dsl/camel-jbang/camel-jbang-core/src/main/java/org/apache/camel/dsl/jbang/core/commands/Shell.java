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
package org.apache.camel.dsl.jbang.core.commands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.jline.builtins.ClasspathResourceUtil;
import org.jline.builtins.ConfigurationPath;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.Builtins;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.Parser;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.jline.widget.TailTipWidgets;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;

@CommandLine.Command(name = "shell",
                     description = "Interactive Camel JBang shell. Hit @|magenta <TAB>|@ to see available commands.",
                     footer = "Press Ctrl-C to exit.")
public class Shell extends CamelCommand {

    public Shell(CamelJBangMain main) {
        super(main);
    }

    @Override
    public Integer doCall() throws Exception {
        Supplier<Path> workDir = () -> Paths.get(System.getProperty("user.dir"));
        // set up JLine built-in commands
        Path appConfig = ClasspathResourceUtil.getResourcePath("/nano/jnanorc", getClass()).getParent();
        Builtins builtins = new Builtins(workDir, new ConfigurationPath(appConfig, workDir.get()), null) {
            @Override
            public String name() {
                return "built-in";
            }
        };

        PicocliCommands.PicocliCommandsFactory factory = new PicocliCommands.PicocliCommandsFactory();
        PicocliCommands commands = new PicocliCommands(CamelJBangMain.getCommandLine());
        commands.name("Camel");

        try (Terminal terminal = TerminalBuilder.builder().build()) {
            Parser parser = new DefaultParser();
            SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, null);
            systemRegistry.setCommandRegistries(builtins, commands);
            systemRegistry.register("help", commands);

            String history = Paths.get(System.getProperty("user.home"), ".camel-jbang-history").toString();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(systemRegistry.completer())
                    .parser(parser)
                    .highlighter(new ReplHighlighter())
                    .variable(LineReader.LIST_MAX, 50)   // max tab completion candidates
                    .variable(LineReader.HISTORY_FILE, history)
                    .variable(LineReader.OTHERS_GROUP_NAME, "Others")
                    .variable(LineReader.COMPLETION_STYLE_GROUP, "fg:blue,bold")
                    .variable("HELP_COLORS", "ti=1;34:co=38:ar=3:op=33:de=90")
                    .option(LineReader.Option.GROUP_PERSIST, true)
                    .build();
            builtins.setLineReader(reader);
            factory.setTerminal(terminal);
            TailTipWidgets widgets
                    = new TailTipWidgets(reader, systemRegistry::commandDescription, 5, TailTipWidgets.TipType.COMPLETER);
            widgets.enable();
            KeyMap<Binding> keyMap = reader.getKeyMaps().get("main");
            keyMap.bind(new Reference("tailtip-toggle"), KeyMap.alt("s"));
            String prompt = "camel> ";
            String rightPrompt = null;

            // start the shell and process input until the user quits with Ctrl-C or Ctrl-D
            String line;
            boolean run = true;
            TerminalBuilder.setTerminalOverride(terminal);
            while (run) {
                try {
                    systemRegistry.cleanUp();
                    line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                    systemRegistry.execute(line);
                } catch (UserInterruptException e) {
                    // ctrl + c is pressed so exit
                    run = false;
                } catch (EndOfFileException e) {
                    // ctrl + d is pressed so exit
                    run = false;
                } catch (Exception e) {
                    systemRegistry.trace(e);
                }
            }
        } finally {
            TerminalBuilder.setTerminalOverride(null);
        }
        return 0;
    }

    private static class ReplHighlighter extends DefaultHighlighter {
        @Override
        protected void commandStyle(LineReader reader, AttributedStringBuilder sb, boolean enable) {
            if (enable) {
                if (reader.getTerminal().getNumericCapability(InfoCmp.Capability.max_colors) >= 256) {
                    sb.style(AttributedStyle.DEFAULT.bold().foreground(69));
                } else {
                    sb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
                }
            } else {
                sb.style(AttributedStyle.DEFAULT.boldOff().foregroundOff());
            }
        }
    }
}
