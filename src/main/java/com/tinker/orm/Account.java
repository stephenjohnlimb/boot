package com.tinker.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/** Just a simple class that will map directly on to a database table. */
@Entity
@Table(name = "accounts")
public class Account
{

	@Id
	@Column(name = "id")
	public long id;

	public long getId()
	{
		return id;
	}

	@Column(name = "balance")
	public BigDecimal balance;

	public BigDecimal getBalance()
	{
		return balance;
	}

	public void setBalance(BigDecimal newBalance)
	{
		this.balance = newBalance;
	}

	// Convenience constructor.
	public Account(int id, int balance)
	{
		this.id = id;
		this.balance = BigDecimal.valueOf(balance);
	}

	// Hibernate needs a default (no-arg) constructor to create model objects.
	public Account()
	{
	}
}