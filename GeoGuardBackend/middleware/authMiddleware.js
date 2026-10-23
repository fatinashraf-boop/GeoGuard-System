
const jwt = require('jsonwebtoken');

module.exports = function verifyToken(req, res, next) {
    const authorization = req.headers.authorization || '';
    const match = authorization.match(/^Bearer\s+(.+)$/i);

    console.log('[GeoGuard Auth] Authorization header present:', !!authorization);
    console.log('[GeoGuard Auth] Bearer token recognized:', !!match);
    console.log('[GeoGuard Auth] JWT secret configured:', !!process.env.JWT_SECRET);

    if (!match) {
        return res.status(401).json({
            message: 'Authorization token required'
        });
    }

    try {
        req.user = jwt.verify(match[1].trim(), process.env.JWT_SECRET);
        console.log('[GeoGuard Auth] Verified request:', req.method, req.originalUrl);
        console.log('[GeoGuard Auth] JWT verification successful');
        console.log('[GeoGuard Auth] User ID:', req.user.user_id);
        console.log('[GeoGuard Auth] JWT verification successful');
        next();
    } catch (error) {
        console.log('[GeoGuard Auth] JWT verification error:', error.name);
        return res.status(401).json({
            message: 'Invalid or expired token'
        });
    }
};
