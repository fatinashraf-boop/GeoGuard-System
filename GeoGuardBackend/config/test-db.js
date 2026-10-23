
const db = require('./db');

async function testConnection() {
    try {
        const connection = await db.getConnection();
        console.log('MySQL connected successfully!');
        
        const [rows] = await connection.query(
            'SELECT DATABASE() AS databaseName'
        );
        console.log(rows[0]);

        connection.release();
        await db.end();
    } catch (error) {
        console.error('MySQL connection failed:', error.message);
        process.exitCode = 1;
    }
}

testConnection();

