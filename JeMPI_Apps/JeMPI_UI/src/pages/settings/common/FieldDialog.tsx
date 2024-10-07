import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import { useState } from 'react';
import { DialogContentText } from '@mui/material';

interface FieldDialogProps {
    openFieldModal: boolean;
    setOpenFieldModal: React.Dispatch<React.SetStateAction<boolean>>;
    onSave: (fieldName: string) => void;
}

export default function FieldDialog({ openFieldModal, setOpenFieldModal, onSave }: FieldDialogProps) {
    const [fieldName, setFieldName] = useState('');

    const handleClose = () => {
        setOpenFieldModal(false);
    };

    const handleSave = () => {
        onSave(fieldName);
        handleClose();
    };

    return (
        <Dialog fullWidth open={openFieldModal} onClose={handleClose} aria-labelledby="form-dialog-title">
            <DialogTitle id="form-dialog-title">Add Field Name</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    To add a new field to common settings, please enter a field name.
                </DialogContentText>
                <TextField
                    autoFocus
                    margin="dense"
                    id="name"
                    label=""
                    type="text"
                    fullWidth
                    value={fieldName}
                    variant="standard" 
                    onChange={(e) => setFieldName(e.target.value)}
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} color="primary">
                    Cancel
                </Button>
                <Button
                    onClick={handleSave}
                    color="primary"
                    disabled={!fieldName.trim()}
                >
                    Save
                </Button>
            </DialogActions>
        </Dialog>
    );
}
