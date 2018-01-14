/*
 * Copyright 2018 Patrik Karlsson.
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
package se.trixon.idl;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Patrik Karlsson
 */
public enum Command {
    /**
     * Closes the connection to IDD. This command will not generate a response.
     */
    CLOSE,
    /**
     * Add this client as an image broadcast listener.
     */
    DEREGISTER,
    /**
     * Kills IDD.
     */
    KILL,
    /**
     * This is used for authentication with the server. PASSWORD is simply the plaintext password.
     */
    PASSWORD,
    /**
     * Does nothing but return "OK".
     */
    PING,
    /**
     *
     */
    RANDOM,
    /**
     * Add this client as an image broadcast listener.
     */
    REGISTER,
    /**
     *
     */
    STATS,
    /**
     * Updates the music database: find new files, remove deleted files, update modified files.
     *
     * URI is a particular directory or song/file to update. If you do not specify it, everything is
     * updated.
     *
     * Prints "updating_db: JOBID" where JOBID is a positive number identifying the update job. You
     * can read the current job id in the status response.
     */
    UPDATE,
    /**
     * Returns the IDD program version.
     */
    VERSION;
    private static final SortedSet<String> sSet = new TreeSet<>();

    static {
        for (Command command : Command.values()) {
            sSet.add(command.name());
        }
    }

    public static SortedSet<String> getSet() {
        return sSet;
    }

    private Command() {
    }

    public boolean validateArgs(String[] args) {
        boolean valid;

        switch (this) {
            case CLOSE:
            case KILL:
            case PING:
            case RANDOM:
            case REGISTER:
            case STATS:
            case VERSION:
                valid = args.length == 0;
                break;

            case UPDATE:
                valid = args.length >= 0 && args.length < 1;
                break;

            case PASSWORD:
                valid = args.length == 1;
                break;

            default:
                valid = true;
        }

        return valid;
    }
}
