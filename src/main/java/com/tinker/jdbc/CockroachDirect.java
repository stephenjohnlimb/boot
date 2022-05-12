package com.tinker.jdbc;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class CockroachDirect
{
	public static void main(String[] args)
	{
		System.out.println("Sample JDBC example with cockroachdb");
		var ds = new PGSimpleDataSource();

		//Pickup from environment or throw an exception.
		var jdbcUrl = Optional.ofNullable(System.getenv("JDBC_DATABASE_URL"));
		ds.setURL(jdbcUrl.orElseThrow());

		try(Connection connection = ds.getConnection())
		{
			System.out.println("Connection made to " + jdbcUrl);
			try(Statement stmt = connection.createStatement())
			{
				ResultSet results = stmt.executeQuery("SHOW TABLES");
				while(results.next())
				{
					System.out.println("Schema [" + results.getString(1) + "] Table [" + results.getString(2) + "]");
				}
			}
			catch(Exception ex)
			{
				System.err.println("Failed to run statement");
				System.exit(3);
			}

			//Alternatively we could just do this and get much more info.
			DatabaseMetaData metaData = connection.getMetaData();
			String[] types = {"TABLE"};
			//Retrieving the columns in the database
			ResultSet tables = metaData.getTables(null, null, "%", types);
			while(tables.next())
			{
				System.out.println(tables.getString("TABLE_NAME"));
			}

		}
		catch(Exception ex)
		{
			System.err.println("Failed to get connection to cockroach on " + jdbcUrl);
			System.exit(2);
		}
	}
}
