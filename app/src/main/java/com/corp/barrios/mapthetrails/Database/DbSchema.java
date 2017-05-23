package com.corp.barrios.mapthetrails.Database;

import android.provider.BaseColumns;

/**
 * Created by hector on 2/28/17.
 */

public class DbSchema {

    private DbSchema() {}

    public static class DbEntry implements BaseColumns
    {
        public static final String TABLE = "trails";
        public static final String TRAIL = "trail";
        public static final String UUID = "uuid";
    }
}
