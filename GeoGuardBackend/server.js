require('dotenv').config();

const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const authRoutes = require('./routes/authRoutes');
const reportRoutes = require('./routes/reportRoutes');
const verifyToken = require('./middleware/authMiddleware');
const authorizeRoles = require('./middleware/roleMiddleware');

const app = express();

app.use(cors());
app.use(express.json());

// Ensure uploads folder exists
if (!fs.existsSync('./uploads')) {
    fs.mkdirSync('./uploads');
}

// Serve uploaded photos
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

app.get('/', (req, res) => {
    res.json({ message: 'GeoGuard API is running' });
});

// Mounted Routes
app.use('/api/auth', authRoutes);
app.use('/api/reports', reportRoutes);

app.get(
    '/api/admin/test',
    verifyToken,
    authorizeRoles('admin'),
    (req, res) => {
        res.json({
            message: 'Admin access granted!',
            user: req.user
        });
    }
);

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`GeoGuard API running on port ${PORT}`);
});