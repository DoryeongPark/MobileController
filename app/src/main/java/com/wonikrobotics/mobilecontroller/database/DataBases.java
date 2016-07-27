package com.wonikrobotics.mobilecontroller.database;

import android.provider.BaseColumns;

/**
 * Created by Notebook on 2016-07-05.
 */
public final class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String NAME = "name";
        public static final String URI = "uri";
        public static final String _TABLENAME = "robotlist";
        public static final String MASTER = "master";
        public static final String IDX = "idx";
        public static final String _CREATE =
                "create table "+_TABLENAME+" ( "
                        +IDX+" integer primary key autoincrement, "
                        +NAME+" text not null , "
                        +MASTER+" text not null , "
                        +URI+" text not null );";
    }
}
