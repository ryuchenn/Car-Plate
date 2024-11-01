require("dotenv").config();
const express = require("express");
const mongoose = require("mongoose");
const path = require("path");
// const cookieParser = require("cookie-parser");
const bodyParser = require('body-parser');
// const cors = require('cors');
const port = process.env.DB_DEFAULT_PORT;
const app = express();
mongoose.connect(
   "mongodb+srv://" + process.env.DB_USER + ":" + process.env.DB_PASSWORD + "@" +
     process.env.DB_NAME + ".5nu9r.mongodb.net/" + process.env.DB_USE)
 .then(() => console.log("Connect MongoDB Success!"))
 .catch((err) => console.error("Connect MongoDB Error: " + err));

// app.use(cors({ origin: process.env.Frontend_Setting, credentials: true }));
// app.use(cors());
app.use(bodyParser.json());
app.use(express.json({ limit: "15mb", type: 'application/json; charset=UTF-8'  }));
app.use(express.urlencoded({ limit: "15mb", extended: true })); // allowed 50MB for url data
// app.use(cookieParser());

//////////// MongoDB and Mongoose ////////////
const { ExceptionSchema } = require("./models/Exception_md");
const { InfoSchema } = require("./models/Info_md");

//////////// RESTfulAPI Routes && Input Validation && Error Handling ////////////
app.use("/api/action", require("./routes/api"));

//////////// Services ////////////
// const {cleanUpExpiredSessions1, cleanUpExpiredSessions2} = require('./service/cleanupSession'); 
// cleanUpExpiredSessions1(); // per day clear the QR Code login account
// setInterval(cleanUpExpiredSessions2, 3 * 60 * 60 * 1000); // per three hours clear the session

//////////// Testing(Unit Test) ////////////

// Unit test don't run this
app.listen(port, () => {
  console.log(`Server is running at http://localhost:${port}`);
});

// app.listen(port, '0.0.0.0',() => {
//     console.log(`Server is running at http://0.0.0.0:${port}`);
//   });

// Unit test need to export this.
// Expected an Express application object rather than an object that started the server.
module.exports = app;