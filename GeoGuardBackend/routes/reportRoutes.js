const express = require('express');
const multer = require('multer');
const path = require('path');
const db = require('../config/db');
const verifyToken = require('../middleware/authMiddleware');

const router = express.Router();
router.use(verifyToken);

// Configure disk storage for incoming evidence images
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, 'uploads/'),
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, 'evidence-' + uniqueSuffix + path.extname(file.originalname));
  }
});
const upload = multer({ storage });

// GET /api/reports - Fetch all reports for the user (or all if admin)
router.get('/', async (req, res) => {
  try {
    let sql = 'SELECT * FROM incident_reports';
    const args = [];
    if (req.user.role !== 'admin') {
      sql += ' WHERE user_id = ?';
      args.push(req.user.user_id);
    }
    sql += ' ORDER BY created_at DESC';
    const [rows] = await db.execute(sql, args);
    res.json({ reports: rows });
  } catch (e) {
    console.error(e.message);
    res.status(500).json({ message: 'Could not retrieve reports' });
  }
});

// GET /api/reports/:id - Fetch single report details by ID
router.get('/:id', async (req, res) => {
  const id = Number(req.params.id);
  if (!Number.isInteger(id) || id < 1) {
    return res.status(400).json({ message: 'Invalid report ID' });
  }
  try {
    const [rows] = await db.execute('SELECT * FROM incident_reports WHERE report_id = ? LIMIT 1', [id]);
    if (!rows.length) {
      return res.status(404).json({ message: 'Report not found' });
    }
    if (req.user.role !== 'admin' && Number(rows[0].user_id) !== Number(req.user.user_id)) {
      return res.status(403).json({ message: 'Forbidden' });
    }
    res.json({ report: rows[0] });
  } catch (e) {
    console.error(e.message);
    res.status(500).json({ message: 'Could not retrieve report' });
  }
});

// POST /api/reports - Create a new incident report with image upload support
router.post('/', upload.single('image'), async (req, res) => {
  const { title, description, incident_type, latitude, longitude, address } = req.body || {};
  
  if (!title || !description || !incident_type) {
    return res.status(400).json({ message: 'title, description and incident_type are required' });
  }

  const coord = (v, min, max) => 
    v === null || v === undefined || v === '' || 
    (Number.isFinite(Number(v)) && Number(v) >= min && Number(v) <= max);

  if (!coord(latitude, -90, 90) || !coord(longitude, -180, 180)) {
    return res.status(400).json({ message: 'Invalid coordinates' });
  }

  const image_path = req.file ? `/uploads/${req.file.filename}` : (req.body.image_path || null);

  try {
    const [r] = await db.execute(
      'INSERT INTO incident_reports(user_id, title, description, incident_type, latitude, longitude, address, image_path) VALUES(?,?,?,?,?,?,?,?)',
      [
        req.user.user_id,
        String(title).trim(),
        String(description).trim(),
        String(incident_type).trim(),
        latitude === '' || latitude === undefined ? null : latitude,
        longitude === '' || longitude === undefined ? null : longitude,
        address || null,
        image_path
      ]
    );

    const [rows] = await db.execute('SELECT * FROM incident_reports WHERE report_id = ?', [r.insertId]);
    res.status(201).json({ message: 'Report created successfully', report: rows[0] });
  } catch (e) {
    console.error(e.message);
    res.status(500).json({ message: 'Could not create report' });
  }
});

// PUT /api/reports/:id/status - Admin endpoint to update report status
router.put('/:id/status', async (req, res) => {
  const id = Number(req.params.id);
  const { status } = req.body || {};

  if (!Number.isInteger(id) || id < 1) {
    return res.status(400).json({ message: 'Invalid report ID' });
  }

  const validStatuses = ['submitted', 'in_progress', 'resolved', 'rejected'];
  if (!status || !validStatuses.includes(String(status).toLowerCase())) {
    return res.status(400).json({ message: 'Invalid status value provided' });
  }

  // Ensure only admin users can update report statuses
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'Forbidden: Admin access required' });
  }

  try {
    const [result] = await db.execute(
      'UPDATE incident_reports SET status = ? WHERE report_id = ?',
      [status.toLowerCase(), id]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'Report not found' });
    }

    const [rows] = await db.execute('SELECT * FROM incident_reports WHERE report_id = ?', [id]);
    res.json({ message: 'Status updated successfully', report: rows[0] });
  } catch (e) {
    console.error(e.message);
    res.status(500).json({ message: 'Could not update report status' });
  }
});

module.exports = router;