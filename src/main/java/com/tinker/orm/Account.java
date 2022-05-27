package com.tinker.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/** Just a simple class that will map directly on to a database table. */
@Entity
@Table(name = "accounts")
public final class Account
{
	@Id
	@Column(name = "id")
	public long id;

	@Column(name = "balance")
	public BigDecimal balance;

	// Hibernate needs a default (no-arg) constructor to create model objects.
	public Account()
	{
	}

	// Convenience constructor.
	public Account(int id, int balance)
	{
		this.id = id;
		this.balance = BigDecimal.valueOf(balance);
	}

	public long getId()
	{
		return id;
	}

	public BigDecimal getBalance()
	{
		return balance;
	}

	public void setBalance(BigDecimal newBalance)
	{
		this.balance = newBalance;
	}
}