/*
 * Copyright 2016 Patrik Karlsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.idd;

import se.trixon.idl.shared.Commands;
import se.trixon.util.SystemHelper;

/**
 *
 * @author Patrik Karlsson
 */
public class Executor {

    private final String[] mArgs;
    private final String mCommand;

    public Executor(String command, String... args) {
        mCommand = command;
        mArgs = args;
    }

    String execute() {
        System.out.format("execute: %s\n", mCommand);
        String result = null;

        switch (mCommand) {
            case Commands.STATS:
                result = "stats";
                break;
            case Commands.UPDATE:
                result = "update";
                break;
            case Commands.VERSION:
                result = String.format("idd version: %s", SystemHelper.getJarVersion(getClass()));
                break;

        }

        return result;
    }

}
