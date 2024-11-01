const express = require("express");
const router = express.Router();

router.get("/test", (req, res) => {
    console.log(123)
})

// Example route to create a new document in Info collection
router.post('/info', async (req, res) => {
    try {
        const info = new Info(req.body);
        await info.save();
        res.status(201).json(info);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Example route to create a new document in Exception collection
router.post('/exception', async (req, res) => {
    try {
        const exception = new Exception(req.body);
        await exception.save();
        res.status(201).json(exception);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;