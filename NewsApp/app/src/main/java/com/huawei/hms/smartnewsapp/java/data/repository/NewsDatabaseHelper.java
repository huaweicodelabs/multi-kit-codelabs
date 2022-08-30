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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.huawei.hms.smartnewsapp.java.data.model.Article;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Sqlite helper
 */
public class NewsDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SmartNews.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE "
                    + SmartNewsContract.NewsEntry.TABLE_NAME
                    + " ("
                    + SmartNewsContract.NewsEntry._ID
                    + " INTEGER PRIMARY KEY,"
                    + SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_TITLE
                    + " TEXT,"
                    + SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_URL
                    + " TEXT,"
                    + SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_JSON
                    + " TEXT, UNIQUE("
                    + SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_TITLE
                    + ","
                    + SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_URL
                    + "))";
    private static final String SQL_FETCH_ENTRIES = "SELECT * FROM " + SmartNewsContract.NewsEntry.TABLE_NAME;
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + SmartNewsContract.NewsEntry.TABLE_NAME;
    private Gson gson;

    public NewsDatabaseHelper(@Nullable Context context, Gson gson) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.gson = gson;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE);
    }

    /**
     * To save selected news in db
     *
     * @param newsArticle to insert article column
     * @return boolean value based on the insert status
     */
    public boolean insertNews(Article newsArticle) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        newsArticle.setArticleSaved(true);
        values.put(SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_TITLE, newsArticle.getTitle());
        values.put(SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_URL, newsArticle.getUrl());
        values.put(SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_JSON, gson.toJson(newsArticle));
        // Insert the new row, returning the primary key value of the new row
        long newRowId =
                db.insertWithOnConflict(
                        SmartNewsContract.NewsEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return newRowId != -1;
    }

    /**
     * Query to fetch data
     *
     * @return list of article
     */
    public List<Article> getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Article> articleList = new ArrayList<>();
        Cursor cursor = db.rawQuery(SQL_FETCH_ENTRIES, null);
        while (cursor.moveToNext()) {
            articleList.add(gson.fromJson(cursor.getString(3), Article.class));
        }
        cursor.close();
        return articleList;
    }

    /**
     * To delete selected article in db
     *
     * @param article to delete from db
     * @return integer
     *
     */
    public int deleteNewsArticle(Article article) {
        SQLiteDatabase db = this.getWritableDatabase();
        int delete =
                db.delete(
                        SmartNewsContract.NewsEntry.TABLE_NAME,
                        SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_URL
                                + "=? and "
                                + SmartNewsContract.NewsEntry.COLUMN_NAME_ARTICLE_TITLE
                                + "=?",
                        new String[] {article.getUrl(), article.getTitle()});
        db.close();
        return delete;
    }
}
