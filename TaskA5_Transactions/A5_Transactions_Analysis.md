# TASK A5 – CORRECTNESS AND SAFETY

**Course:** Programming in Java  
**Assignment:** Assignment 5 – CO5  
**Topic:** Data Persistence Engineering – Files and Databases  
**Task:** A5 – Correctness and Safety  
**Bloom's Level:** K4 – Analyse  
**Marks:** 4  

---

## 1. JDBC Transaction – ₹500 Wallet Transfer

Consider a wallet-transfer operation where ₹500 is transferred from one user's wallet to another user's wallet.

The operation contains two database updates:

```text
Sender wallet:
Balance = Balance - ₹500

Receiver wallet:
Balance = Balance + ₹500
```

Both operations must succeed together.

If the sender's balance is reduced but the receiver's balance is not increased, the database becomes inconsistent.

Therefore, the two operations should be executed inside a single JDBC transaction.

---

## 2. Transaction Processing

The transaction can be implemented using:

```java
connection.setAutoCommit(false);
```

Example:

```java
try {

    connection.setAutoCommit(false);

    PreparedStatement debit =
        connection.prepareStatement(
            "UPDATE wallet SET balance = balance - ? WHERE user_id = ?"
        );

    debit.setDouble(1, 500.0);
    debit.setInt(2, senderId);
    debit.executeUpdate();

    PreparedStatement credit =
        connection.prepareStatement(
            "UPDATE wallet SET balance = balance + ? WHERE user_id = ?"
        );

    credit.setDouble(1, 500.0);
    credit.setInt(2, receiverId);
    credit.executeUpdate();

    connection.commit();

} catch (Exception e) {

    connection.rollback();

}
```

The important sequence is:

```text
BEGIN TRANSACTION
       ↓
Debit ₹500
       ↓
Credit ₹500
       ↓
Both successful?
   ↙         ↘
 YES          NO
  ↓            ↓
COMMIT       ROLLBACK
```

---

## 3. Why Transactions Are Required

A transaction provides **atomicity**.

Atomicity means that a group of related operations is treated as one unit.

For the wallet transfer:

```text
Debit succeeds + Credit succeeds
        ↓
      COMMIT
```

If either operation fails:

```text
Debit succeeds + Credit fails
        ↓
      ROLLBACK
```

The debit is undone.

Therefore, the database does not end up with money removed from one wallet without being added to the other.

---

## 4. SQL Injection Problem

Consider an unsafe login query created using string concatenation:

```java
String sql =
    "SELECT * FROM users WHERE username = '"
    + username
    + "' AND password = '"
    + password
    + "'";
```

The user input becomes part of the SQL command itself.

For example, a malicious input such as:

```text
' OR '1'='1
```

can alter the meaning of the SQL condition.

This is a SQL injection vulnerability because untrusted input is directly combined with SQL syntax.

---

## 5. Safe Login Using PreparedStatement

The safer approach is to use `PreparedStatement`.

```java
String sql =
    "SELECT * FROM users WHERE username = ? AND password = ?";

PreparedStatement ps =
    connection.prepareStatement(sql);

ps.setString(1, username);
ps.setString(2, password);

ResultSet rs = ps.executeQuery();
```

Here, the SQL structure and user input are handled separately.

The input is treated as parameter data rather than SQL syntax.

### Comparison

| Unsafe Approach | Safe Approach |
|---|---|
| String concatenation | `PreparedStatement` |
| User input becomes part of SQL | User input is bound as data |
| Vulnerable to SQL injection | Strongly reduces SQL injection risk |
| Harder to maintain | Cleaner and reusable |
| Unsafe for untrusted input | Recommended for parameterized queries |

---

## 6. Alternative – Auto-Commit

An alternative is to leave JDBC auto-commit enabled:

```java
connection.setAutoCommit(true);
```

Each SQL statement is committed automatically.

For the wallet transfer:

```text
Debit statement
    ↓
Automatically committed

Credit statement
    ↓
Automatically committed
```

If the credit operation fails after the debit has already committed, the debit cannot automatically be rolled back as part of the same transaction.

This can leave the database in an inconsistent state.

Therefore, auto-commit is not appropriate for a multi-step wallet transfer that must succeed or fail as one unit.

---

## 7. Alternative – Input Escaping or Regular Expressions

Another possible approach is to validate or escape user input.

For example, an application might reject unexpected characters using validation rules.

However, validation alone should not replace parameterized SQL.

A better approach is:

```text
Input validation
       +
PreparedStatement
       ↓
Safer database operation
```

`PreparedStatement` should be the primary protection against SQL injection for parameterized database queries.

---

## 8. Complexity Analysis

For a wallet transfer, assume the database uses an indexed primary key for `user_id`.

The two update operations are typically approximately:

```text
O(1) average per indexed lookup
```

Therefore, the transaction performs a constant number of database operations:

```text
O(1) average
```

The actual performance depends on the database engine, indexes, locks, transaction isolation level, and system load.

---

## 9. Final Engineering Decision

For a ₹500 wallet transfer:

- Disable auto-commit.
- Start one transaction.
- Execute the debit operation.
- Execute the credit operation.
- Commit only when both operations succeed.
- Roll back if any operation fails.

For user authentication:

- Do not concatenate user input into SQL.
- Use `PreparedStatement`.
- Validate input where appropriate.
- Never rely only on escaping or regular expressions for SQL injection protection.

### Final Design

```text
             WALLET TRANSFER

                BEGIN
                  ↓
          Debit ₹500 from A
                  ↓
          Credit ₹500 to B
                  ↓
             Both succeed?
             /           \
           YES            NO
            ↓              ↓
         COMMIT         ROLLBACK
```

This design provides better correctness, consistency, and security than independent auto-committed statements and concatenated SQL queries.