package com.huawei.hms.smartnewsapp.java.data.repository;

/*
 *
 *  * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

import android.provider.BaseColumns;


public class SmartNewsContract {
    private SmartNewsContract() {}

    public static class NewsEntry implements BaseColumns {
        /**
         * Table name i db
         */
        public static final String TABLE_NAME = "saved_news";
        /**
         * column name
         */
        public static final String COLUMN_NAME_ARTICLE_JSON = "article_json";
        /**
         * article title
         */
        public static final String COLUMN_NAME_ARTICLE_TITLE = "article_title";
        /**
         * article url
         */
        public static final String COLUMN_NAME_ARTICLE_URL = "article_url";
    }
}
