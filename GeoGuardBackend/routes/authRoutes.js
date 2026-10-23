const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('../config/db');
const verifyToken = require('../middleware/authMiddleware');

const router = express.Router();

// POST /api/auth/register - Register new user
router.post('/register', async (req, res) => {
  try {
    const { username, email, password } = req.body || {};
    if (!username || !email || !password) {
      return res.status(400).json({ message: 'username, email and password are required' });
    }
    if (String(password).length < 8) {
      return res.status(400).json({ message: 'Password must be at least 8 characters' });
    }
    const hash = await bcrypt.hash(password, 12);
    const [r] = await db.execute(
      'INSERT INTO users(username, email, password_hash, role) VALUES(?,?,?,?)',
      [String(username).trim(), String(email).trim().toLowerCase(), hash, 'inspector']
    );
    res.status(201).json({ message: 'Registration successful', user_id: r.insertId });
  } catch (e) {
    if (e.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ message: 'Username or email already exists' });
    }
    console.error(e.message);
    res.status(500).json({ message: 'Registration failed' });
  }
});

// POST /api/auth/login - Login with Username or Email
router.post('/login', async (req, res) => {
  try {
    const { username, email, password } = req.body || {};
    const identifier = (username || email || '').trim().toLowerCase();

    if (!identifier || !password) {
      return res.status(400).json({ message: 'Username/Email and password are required' });
    }

    const [rows] = await db.execute(
      'SELECT user_id, username, email, password_hash, role FROM users WHERE LOWER(username) = ? OR LOWER(email) = ? LIMIT 1',
      [identifier, identifier]
    );

    if (!rows.length || !(await bcrypt.compare(password, rows[0].password_hash))) {
      return res.status(401).json({ message: 'Invalid email/username or password' });
    }

    const u = rows[0];
    const token = jwt.sign(
      { user_id: u.user_id, username: u.username, email: u.email, role: u.role },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '8h' }
    );

    res.json({
      message: 'Login successful',
      token,
      user: { user_id: u.user_id, username: u.username, email: u.email, role: u.role }
    });
  } catch (e) {
    console.error(e.message);
    res.status(500).json({ message: 'Login failed' });
  }
});

// GET /api/auth/me - Verify session token
router.get('/me', verifyToken, async (req, res) => {
  try {
    const [rows] = await db.execute('SELECT user_id, username, email, role FROM users WHERE user_id=? LIMIT 1', [req.user.user_id]);
    if (!rows.length) {
      return res.status(401).json({ message: 'User no longer exists' });
    }
    res.json({ message: 'Token verified successfully', user: rows[0] });
  } catch (e) {
    console.error(e.message);
    res.status(500).json({ message: 'Could not load user' });
  }
});

// DIRECT ROUTER EXPORT
module.exports = router;