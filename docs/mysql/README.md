# MySQL setup

The app uses `spring.profiles.active=mysql` by default.

Default connection:

- Database: `shangluhua`
- Host: `localhost:3306`
- User: `root`
- Password: `123456`

If your password is different, set an environment variable before starting:

```powershell
$env:MYSQL_PASSWORD='your-password'
```

Run this SQL manually if you want to create the database before starting:

```sql
source E:/IDEAworkplace/ShangLuHua/docs/mysql/01-create-database.sql;
```

Tables are created by JPA. Demo products, SKUs, inventory and customers are inserted automatically when the product table is empty.