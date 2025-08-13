// server.js
const express = require('express');
const bodyParser = require('body-parser');

const app = express();
const port = 3000;

// Middleware to parse JSON bodies
app.use(bodyParser.json());

// Dummy user data
const userProfile = {
  id: 'user123',
  name: 'Muhammad Naufal Ghifari',
  email: 'naufal@mail.com'
};

// API endpoint to get user profile
app.get('/user/profile', (req, res) => {
  console.log('GET request received for /user/profile');
  res.status(200).json(userProfile);
});

// API endpoint to update user profile
app.put('/user/profile/update', (req, res) => {
  console.log('PUT request received for /user/profile/update');
  const { name, email, password } = req.body;

  // Basic validation
  if (!name || !email) {
    return res.status(400).json({ error: 'Name and email are required.' });
  }

  // Update dummy user data
  userProfile.name = name;
  userProfile.email = email;
  
  // Note: Password would be handled securely in a real app (e.g., hashing)
  // For this example, we'll just log that it was received.
  if (password) {
    console.log('Password received for update.');
  }

  res.status(200).json({
    message: 'User profile updated successfully!',
    user: userProfile
  });
});

// Start the server
app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});