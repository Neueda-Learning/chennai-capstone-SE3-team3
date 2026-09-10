const express = require('express');
const jwt = require('jsonwebtoken');

const app = express();
app.use(express.json());

// Shared secret - the mission service (Java) validates tokens signed with
// this exact string. In a real system this would come from a secrets
// manager, never be hardcoded, and never be the same value in two
// unrelated services - here it's deliberately visible so the group can see
// EXACTLY what "the two services agree on a secret" means in practice.
const SECRET = process.env.JWT_SECRET || 'dev-only-change-me-this-secret-is-not-for-production-use';

// A stub, not a real user store - two hardcoded accounts is enough to
// demonstrate "valid token in, protected data out" and "no token, or the
// wrong one, in -> rejected".
const USERS = {
  alice: { accountId: 1001, password: 'mission123', roles: ['MISSION_OPERATOR'] },
  bob: { accountId: 1004, password: 'wrongpermissions', roles: ['GUEST'] },
};

app.post('/login', (req, res) => {
  const { username, password } = req.body || {};
  const user = USERS[username];
  if (!user || user.password !== password) {
    return res.status(401).json({ error: 'invalid username or password' });
  }
  const token = jwt.sign(
    { sub: username, accountId: user.accountId, roles: user.roles },
    SECRET,
    { algorithm: 'HS256', expiresIn: '1h' }
  );
  res.json({ token, accountId: user.accountId, roles: user.roles });
});

app.get('/health', (req, res) => res.json({ status: 'up' }));

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => {
  console.log(`mission-auth-stub listening on http://localhost:${PORT}`);
  console.log(`Try: curl -X POST http://localhost:${PORT}/login -H "Content-Type: application/json" -d '{"username":"alice","password":"mission123"}'`);
  console.log('Expected backend request header: Authorization: Bearer <token>');
});
