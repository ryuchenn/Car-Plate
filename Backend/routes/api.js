const express = require("express");
const router = express.Router();
const Info = require("../models/Info_md");
const moment = require('moment');

// Insert the entry label
router.post('/saveDetect', async (req, res) => {
    try {
        const infoData = req.body;
        if (infoData.EntryTime) {
            const timeString = infoData.EntryTime;
            infoData.EntryTime = moment(timeString, "HH:mm:ss").toDate(); 
        }

        if (infoData.Picture) {
            const base64Image = infoData.Picture;
            const imageBuffer = Buffer.from(base64Image, 'base64'); 
            infoData.Picture = imageBuffer;
        }

        if (infoData.PictureThumbnail) {
            const base64Image = infoData.PictureThumbnail;
            const imageBuffer = Buffer.from(base64Image, 'base64');
            infoData.PictureThumbnail = imageBuffer;
        }

        // console.log(infoData)
        const newInfo = new Info(infoData);
        await newInfo.save();

        res.status(201).json({ message: 'Data saved successfully' });
        console.log("Data saved:", infoData);
    } catch (error) {
        console.error("Error saving data:", error);
        res.status(500).json({ error: 'An error occurred while saving data' });
    }
});

// Get all vehicle
router.get('/vehicle', async (req, res) => {
    try {
        const vehicles = await Info.find({ IsPaid: false });
        // Format the data to include base64-encoded images
        const response = vehicles.map(vehicle => ({
            _id: vehicle._id,
            licensePlate: vehicle.LabelName ? vehicle.LabelName: "",
            entryTime: vehicle.EntryTime,
            picture: vehicle.Picture ? vehicle.Picture.toString('base64').trim() : "",
            pictureThumbnail: vehicle.PictureThumbnail ? vehicle.PictureThumbnail.toString('base64').trim() : "",
        }));

        res.json(response);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Failed to fetch vehicle data' });
    }
});

// Search Function: Get specific vehicle
router.get('/searchVehicles', async (req, res) => {
    try {
        const { labelName } = req.query;
        
        // Define search conditions
        const searchConditions = {
            LabelName: { $regex: labelName, $options: 'i' }, // Case-insensitive partial match
            IsPaid: 0,
            IsOut: 0,
        };

        // Query MongoDB
        const vehicles = await Info.find(searchConditions);

        const transformedVehicles = vehicles.map(vehicle => ({
            _id: vehicle._id,
            licensePlate: vehicle.LabelName, 
            entryTime: vehicle.EntryTime,      
            picture: vehicle.Picture ? vehicle.Picture.toString('base64').trim() : "",
            pictureThumbnail: vehicle.PictureThumbnail ? vehicle.PictureThumbnail.toString('base64').trim() : "",
            // paidStatus: vehicle.IsPaid,       
            // exitStatus: vehicle.IsOut, 
        }));

        res.json(transformedVehicles);
    } catch (error) {
        console.error("Error fetching vehicles:", error);
        res.status(500).json({ error: "An error occurred while fetching vehicles" });
    }
});

// Update the payment status when customer already pay the parking fee
router.put('/updatePayment/:id', async (req, res) => {
    try {
        const { id } = req.params; // Document ID
        const { total, parkingHours } = req.body; // Data sent from Android

        const updateData = {
            IsPaid: true,
            Total: total,
            ParkingHours: parkingHours,
        };

        const updatedInfo = await Info.findByIdAndUpdate(id, updateData, { new: true });

        if (updatedInfo) {
            res.status(200).json({ message: 'Payment updated successfully', data: updatedInfo });
        } else {
            res.status(404).json({ error: 'Info not found' });
        }
    } catch (error) {
        console.error("Error updating payment:", error);
        res.status(500).json({ error: 'An error occurred while updating payment' });
    }
});

router.get('/test', async (req, res) => {
    console.log(123)
});

module.exports = router;