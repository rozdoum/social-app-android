/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rozdoum.socialcomponents;

/**
 * Created by alexey on 08.12.16.
 */

public class Constants {

    public static class Profile {
        public static final int MAX_AVATAR_SIZE = 1280; //px, side of square
        public static final int MIN_AVATAR_SIZE = 100; //px, side of square
        public static final int MAX_NAME_LENGTH = 120;
    }

    public static class Post {
        public static final int MAX_TEXT_LENGTH_IN_LIST = 300; //characters
        public static final int MAX_POST_TITLE_LENGTH = 255; //characters
        public static final int POST_AMOUNT_ON_PAGE = 10;
    }

    public static class Database {
        public static final int MAX_UPLOAD_RETRY_MILLIS = 60000; //1 minute
    }

    public static class PushNotification {
        public static final int LARGE_ICONE_SIZE = 256; //px
    }


    public static class General {
        public static final long DOUBLE_CLICK_TO_EXIT_INTERVAL = 3000; // in milliseconds
    }
}
