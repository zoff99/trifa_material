/* SPDX-License-Identifier: GPL-3.0-or-later
 * [sorma2], Java part of sorma2
 * Copyright (C) 2024 Zoff <zoff@zoff.cc>
 */

 package com.zoffcc.applications.sorm;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.zoffcc.applications.sorm.Log;

public class OrmaDatabase
{
    private static final String TAG = "sorm.OrmaDatabase";
    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean ORMA_LONG_RUNNING_TRACE = true; // set "false" for release builds
    final static long ORMA_LONG_RUNNING_MS = 180;

    public static Connection sqldb = null;
    static int current_db_version = 0;
    static Semaphore orma_semaphore_lastrowid_on_insert = new Semaphore(1);

    private static String db_file_path = null;
    private static String secrect_key = null;
    private static boolean wal_mode = false; // default mode is WAL off!

    public OrmaDatabase(final String db_file_path, final String secrect_key, boolean wal_mode)
    {
        OrmaDatabase.db_file_path = db_file_path;
        OrmaDatabase.secrect_key = secrect_key;
        OrmaDatabase.wal_mode = wal_mode;
    }

    public static Connection getSqldb()
    {
        return sqldb;
    }

    public static String bytesToString(byte[] bytes)
    {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String sha256sum_of_file(String filename_with_path)
    {
        try
        {
            byte[] buffer = new byte[8192];
            int count;
            long bytes_read_total = 0;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename_with_path));
            while ((count = bis.read(buffer)) > 0)
            {
                digest.update(buffer, 0, count);
                bytes_read_total = bytes_read_total + count;
            }
            bis.close();
            Log.i(TAG, "sha256sum_of_file:bytes_read_total=" + bytes_read_total);
            byte[] hash = digest.digest();
            return (bytesToString(hash));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static final int BINDVAR_TYPE_Int = 0;
    static final int BINDVAR_TYPE_Long = 1;
    static final int BINDVAR_TYPE_String = 2;
    static final int BINDVAR_TYPE_Boolean = 3;
    static final int BINDVAR_OFFSET_WHERE = 400;
    static final int BINDVAR_OFFSET_SET = 600;

    public static class OrmaBindvar
    {
        int type;
        Object value;

        OrmaBindvar(final int type, final Object value)
        {
            this.type = type;
            this.value = value;
        }
    }

    /*
     * repair or finally replace a string that is not correct UTF-8
     */
    @Deprecated
    static String safe_string_sql(String in)
    {
        if (in == null)
        {
            return null;
        }

        if (in.equals(""))
        {
            return "";
        }

        try
        {
            byte[] bytes = in.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < bytes.length; i++)
            {
                if (bytes[i] == 0)
                {
                    bytes[i] = '_';
                }
            }
            return (new String(bytes, StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            Log.i(TAG, "safe_string_sql:EE:" + e.getMessage());
            e.printStackTrace();
        }
        return "__ERROR_IN_STRING__";
    }

    public static long get_last_rowid_pstmt()
    {
        try
        {
            long ret = -1;
            PreparedStatement lastrowid_pstmt = sqldb.prepareStatement("select last_insert_rowid() as lastrowid");
            ResultSet rs = lastrowid_pstmt.executeQuery();
            if (rs.next())
            {
                ret = rs.getLong("lastrowid");
            }
            rs.close();
            lastrowid_pstmt.close();
            // Log.i(TAG, "get_last_rowid_pstmt:ret=" + ret);
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_last_rowid_pstmt:EE1:" + e.getMessage());
            return -1;
        }
    }

    public static long get_last_rowid(Statement statement)
    {
        try
        {
            long ret = -1;
            ResultSet rs = statement.executeQuery("select last_insert_rowid() as lastrowid");
            if (rs.next())
            {
                ret = rs.getLong("lastrowid");
            }
            // Log.i(TAG, "get_last_rowid:ret=" + ret);
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_last_rowid:EE1:" + e.getMessage());
            return -1;
        }
    }

    /*
     * escape to prevent SQL injection, very basic and bad!
     * TODO: make me better (and later use prepared statements)
     */
    @Deprecated
    public static String s(String str)
    {
        // TODO: bad!! use prepared statements
        String data = "";

        str = safe_string_sql(str);

        if (str == null || str.length() == 0)
        {
            return "";
        }

        if (str != null && str.length() > 0)
        {
            str = str.
                    // replace("\\", "\\\\"). // \ -> \\
                    // replace("%", "\\%"). // % -> \%
                    // replace("_", "\\_"). // _ -> \_
                            replace("'", "''"). // ' -> ''
                            replace("\\x1a", "\\Z"); // \\x1a --> EOF char
            data = str;
        }

        return data;
    }

    public static String s(int i)
    {
        return "" + i;
    }

    public static String s(long l)
    {
        return "" + l;
    }

    public static int b(boolean in)
    {
        if (in == true)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static String readSQLFileAsString(String filePath) throws java.io.IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line, results = "";
        while ((line = reader.readLine()) != null)
        {
            results += line;
        }
        reader.close();
        return results;
    }

    public static String get_current_sqlite_version()
    {
        String ret = "unknown";

        try
        {
            final Statement statement = sqldb.createStatement();
            final ResultSet rs = statement.executeQuery("SELECT sqlite_version()");
            if (rs.next())
            {
                ret = rs.getString(1);
            }

            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public static int get_current_db_version()
    {
        int ret = 0;

        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "select db_version from orma_schema order by db_version desc limit 1");
            if (rs.next())
            {
                ret = rs.getInt("db_version");
            }

            try
            {
                statement.close();
            }
            catch (Exception ignored)
            {
            }

            return ret;
        }
        catch (Exception e)
        {
            ret = 0;

            try
            {
                final String update_001 = "CREATE TABLE orma_schema (db_version INTEGER NOT NULL);";
                run_multi_sql(update_001);
                final String update_002 = "insert into orma_schema values ('0');";
                run_multi_sql(update_002);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }

        return ret;
    }

    public static void set_new_db_version(int new_version)
    {
        try
        {
            final String update_001 = "update orma_schema set db_version='" + new_version + "';";
            run_multi_sql(update_001);
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    public static int update_db(final int current_db_version)
    {
        if (current_db_version < 1)
        {
            // dummy. sadly now it has to stay.
        }

        /*
        if (current_db_version < 2)
        {
            try
            {
                final String update_001 =
                        "alter table Message add ft_outgoing_queued BOOLEAN NOT NULL DEFAULT false;" + "\n" +
                                "CREATE INDEX index_ft_outgoing_queued_on_Message ON Message (ft_outgoing_queued);";
                run_multi_sql(update_001);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        */

        final int new_db_version = 1;
        set_new_db_version(new_db_version);
        // return the updated DB VERSION
        return new_db_version;
    }

    public static void shutdown()
    {
        Log.i(TAG, "SHUTDOWN:start");
        try
        {
            sqldb.close();
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            Log.i(TAG, "SHUTDOWN:Error:" + e2.getMessage());
        }
        Log.i(TAG, "SHUTDOWN:finished");
    }

    public static void init()
    {
        Log.i(TAG, "INIT:start");
        // create a database connection
        try
        {
            // Class.forName("org.sqlite.JDBC");
            sqldb = DriverManager.getConnection("jdbc:sqlite:" + OrmaDatabase.db_file_path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "INIT:R_Error:" + e.getMessage());
        }

        if (OrmaDatabase.wal_mode)
        {
            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));

            // set WAL mode
            final String set_wal_mode = "PRAGMA journal_mode = WAL;";
            run_multi_sql(set_wal_mode);
            Log.i(TAG, "INIT:setting WAL mode");

            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));
            Log.i(TAG, "INIT:wal_autocheckpoint=" + run_query_for_single_result("PRAGMA wal_autocheckpoint;"));

            // set journal and wal size limit to 10 MB
            final String set_journal_size_limit = "PRAGMA journal_size_limit = " + (10 * 1024 * 1024) + ";";
            run_multi_sql(set_journal_size_limit);
            Log.i(TAG, "INIT:setting journal_size_limit");

            // set wal_autocheckpoint
            final String set_wal_autocheckpoint = "PRAGMA wal_autocheckpoint = 1000;";
            run_multi_sql(set_wal_autocheckpoint);
            Log.i(TAG, "INIT:setting wal_autocheckpoint");


            Log.i(TAG, "INIT:journal_mode=" + run_query_for_single_result("PRAGMA journal_mode;"));
            Log.i(TAG, "INIT:journal_size_limit=" + run_query_for_single_result("PRAGMA journal_size_limit;"));
            Log.i(TAG, "INIT:wal_autocheckpoint=" + run_query_for_single_result("PRAGMA wal_autocheckpoint;"));
        } else {
            // turn off WAL mode (since this setting will persist inside the database even after a restart)
            final String set_wal_mode = "PRAGMA journal_mode = DELETE;";
            run_multi_sql(set_wal_mode);
            Log.i(TAG, "INIT:turning OFF WAL mode");
        }

        Log.i(TAG, "loaded:sqlite:" + get_current_sqlite_version());

        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        current_db_version = get_current_db_version();
        Log.i(TAG, "trifa:current_db_version=" + current_db_version);
        create_db(current_db_version);
        current_db_version = update_db(current_db_version);
        Log.i(TAG, "trifa:new_db_version=" + current_db_version);
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        // --------------- CREATE THE DATABASE ---------------
        Log.i(TAG, "INIT:finished");
    }

    public static void create_db(int current_db_version)
    {
        try
        {
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Runs SQL statements that are seperated by ";" character
     */
    public static void run_multi_sql(String sql_multi)
    {
        try
        {
            Statement statement = null;

            try
            {
                statement = sqldb.createStatement();
                statement.setQueryTimeout(10);  // set timeout to x sec.
            }
            catch (SQLException e)
            {
                System.err.println(e.getMessage());
                Log.i(TAG, "ERR:MS:001:" + e.getMessage());
            }

            String[] queries = sql_multi.split(";");
            for (String query : queries)
            {
                try
                {
                    // Log.i(TAG, "SQL:" + query);
                    statement.executeUpdate(query);
                }
                catch (SQLException e)
                {
                    System.err.println(e.getMessage());
                    Log.i(TAG, "ERR:MS:002:" + e.getMessage());
                }
            }

            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:MS:003:" + e.getMessage());
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:MS:004:" + e.getMessage());
        }
    }

    public static String run_query_for_single_result(String sql_multi)
    {
        String text_result = null;

        try
        {
            Statement statement = null;

            try
            {
                statement = sqldb.createStatement();
                statement.setQueryTimeout(10);  // set timeout to x sec.
            }
            catch (SQLException e)
            {
                System.err.println(e.getMessage());
                Log.i(TAG, "ERR:QSL:001:" + e.getMessage());
            }

            try
            {
                String[] queries = sql_multi.split(";");
                for (String query : queries)
                {
                    // Log.i(TAG, "SQL:" + query);
                    ResultSet rs = statement.executeQuery(query);
                    if (rs.next())
                    {
                        text_result = rs.getObject(1).toString();
                    }
                }
            }
            catch (SQLException e)
            {
                System.err.println(e.getMessage());
                Log.i(TAG, "ERR:QSL:002:" + e.getMessage());
            }

            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                Log.i(TAG, "ERR:QSL:003:" + e.getMessage());
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "ERR:QSL:004:" + e.getMessage());
        }

        return text_result;
    }

    public static boolean set_bindvars_where(final PreparedStatement statement,
                                             final int bind_where_count,
                                             final List<OrmaBindvar> bind_where_vars)
    {
        try {
            statement.clearParameters();
            if (bind_where_count > 0) {
                try {
                    for (int jj = 0; jj < bind_where_count; jj++) {
                        int type = bind_where_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_WHERE), (int) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_WHERE), (long) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_WHERE), (String) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_WHERE), (boolean) bind_where_vars.get(jj).value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e1)
        {
            return false;
        }
        return true;
    }

    public static boolean set_bindvars_where_and_set(final PreparedStatement statement,
                                                     final int bind_where_count,
                                                     final List<OrmaBindvar> bind_where_vars,
                                                     final int bind_set_count,
                                                     final List<OrmaBindvar> bind_set_vars)
    {
        try {
            statement.clearParameters();
            if (bind_set_count > 0)
            {
                try {
                    for (int jj = 0; jj < bind_set_count; jj++) {
                        int type = bind_set_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_SET),
                                    (int) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_SET),
                                    (long) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_SET),
                                    (String) bind_set_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_SET),
                                    (boolean) bind_set_vars.get(jj).value);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            if (bind_where_count > 0)
            {
                try {
                    for (int jj = 0; jj < bind_where_count; jj++) {
                        int type = bind_where_vars.get(jj).type;
                        if (type == BINDVAR_TYPE_Int) {
                            statement.setInt((jj + BINDVAR_OFFSET_WHERE),
                                    (int) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Long) {
                            statement.setLong((jj + BINDVAR_OFFSET_WHERE),
                                    (long) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_String) {
                            statement.setString((jj + BINDVAR_OFFSET_WHERE),
                                    (String) bind_where_vars.get(jj).value);
                        } else if (type == BINDVAR_TYPE_Boolean) {
                            statement.setBoolean((jj + BINDVAR_OFFSET_WHERE),
                                    (boolean) bind_where_vars.get(jj).value);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e1)
        {
            return false;
        }
        return true;
    }

    public static void log_bindvars_where(final String sql, final int bind_where_count, final List<OrmaBindvar> bind_where_vars)
    {
        if (ORMA_TRACE)
        {
            Log.i(TAG, "sql=" + sql + " bindvar count=" + bind_where_count);
            if (bind_where_count > 0)
            {
                for(int jj=0;jj<bind_where_count;jj++) {
                    Log.i(TAG, "bindvar ?" + (jj + BINDVAR_OFFSET_WHERE) +
                            " = " + bind_where_vars.get(jj).value);
                }
            }
        }
    }

    public static void log_bindvars_where_and_set(final String sql, final int bind_where_count,
                                                  final List<OrmaBindvar> bind_where_vars,
                                                  final int bind_set_count,
                                                  final List<OrmaBindvar> bind_set_vars)
    {
        if (ORMA_TRACE)
        {
            Log.i(TAG, "sql=" + sql + " bindvar count=" + (bind_set_count + bind_where_count));
            if (bind_set_count > 0)
            {
                for(int jj=0;jj<bind_set_count;jj++) {
                    Log.i(TAG, "bindvar set ?" + (jj + BINDVAR_OFFSET_SET) +
                            " = " + bind_set_vars.get(jj).value);
                }
            }
            if (bind_where_count > 0)
            {
                for(int jj=0;jj<bind_where_count;jj++) {
                    Log.i(TAG, "bindvar where ?" + (jj + BINDVAR_OFFSET_WHERE) +
                            " = " + bind_where_vars.get(jj).value);
                }
            }
        }
    }

    // ______@@0001@@______
}

