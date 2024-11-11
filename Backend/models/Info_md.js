const mongoose = require('mongoose');

const InfoSchema = new mongoose.Schema({
    LabelName: { type: String, required: false },
    EntryTime: { type: Date, required: false },
    OutTime: { type: Date, required: false },
    IsOut: { type: Boolean, default: false },
    ParkingHours: { type: String, required: false },
    IsPaid: { type: Boolean, default: false },
    Total: { type: Number, required: false },
    Picture: { type: Buffer, required: false },
    PictureThumbnail: { type: Buffer, required: false }
});

module.exports = mongoose.model('Info', InfoSchema, 'Info');
