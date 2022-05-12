package com.tinker.orm;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

public class CockroachORM
{
	public static void main(String[] args)
	{
		//Note we pickup the JDBC URL from the environment here.
		//Setup the session factory and register our Account bean, this will automatically create the table
		//We would not normally use this, but as this a quick refresher we'll leave that as is.

		try(SessionFactory sessionFactory
						= new Configuration()
				.configure("hibernate.cfg.xml")
				.setProperty("hibernate.connection.url", Optional.ofNullable(System.getenv("JDBC_DATABASE_URL")).orElseThrow())
				.addAnnotatedClass(Account.class)
				.buildSessionFactory())
		{

			System.out.println("Created Hibernate Session Factory");

			try(Session session = sessionFactory.openSession())
			{
				var result = Optional.of(session).stream().map(addAccounts()).reduce(BigDecimal.ZERO, BigDecimal::add);
				System.out.printf("Result is %.2f\n", result);
			}
		}
		catch(HibernateException e)
		{
			System.err.println("Failed to create hibernate session factory");
		}
	}

	/**
	 * Higher order function method, to return a function that adds the accounts.
	 */
	private static Function<Session, BigDecimal> addAccounts() throws JDBCException
	{
		return s -> {
				s.save(new Account(1, 1000));
				s.save(new Account(2, 250));
				s.save(new Account(3, 314159));
				System.out.println("APP: addAccounts()");
			return BigDecimal.valueOf(1);
		};
	}
}
