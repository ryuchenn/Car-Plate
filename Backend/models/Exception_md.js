const mongoose = require('mongoose');

const ExceptionSchema = new mongoose.Schema({
    OriginalLabelName: { type: String, required: false },
    EditedLabelName: { type: String, required: false },
    IsExport: { type: Boolean, default: false },
    ExportDate: { type: Date, required: false },
    Picture: { type: Buffer, required: false }
});

module.exports = mongoose.model('Exception', ExceptionSchema, 'Exception');
