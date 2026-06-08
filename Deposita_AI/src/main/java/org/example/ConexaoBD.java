package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {

    private static final String URL      = "jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:5432/postgres?user=postgres.indtcdckbfdlwzhspklu&password=nNxiJ6mpt4smotmR";
    private static final String USER     = "postgres.indtcdckbfdlwzhspklu";
    private static final String PASSWORD = "nNxiJ6mpt4smotmR";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}