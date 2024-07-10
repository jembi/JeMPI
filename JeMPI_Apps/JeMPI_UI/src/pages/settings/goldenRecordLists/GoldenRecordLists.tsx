import { useEffect, useState} from 'react';
import Box from '@mui/material/Box';
import {
  DataGrid,
  GridColDef,
  GridEventListener,
  GridRowEditStopReasons,
  GridRowId,
  GridRowModel,
  GridRowModes,
  GridRowModesModel,
  GridActionsCellItem,
} from '@mui/x-data-grid';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Close';
import { EditToolbar } from 'components/shared/EditToolBar';
import { formatNodeName, toSnakeCase, toUpperCase } from 'utils/helpers';
import { Configuration, CustomNode } from 'types/Configuration';
import { useConfiguration } from 'hooks/useUIConfiguration';

interface RowData {
  id: string;
  nodeName: string;
  fieldName: string;
  fieldType: string;
  csvCol: number;
  nodeIndex: number;
  fieldIndex: number;
}


const GoldenRecordLists = () => {
  const [rows, setRows] = useState<RowData[]>([]);
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({});
  const { configuration, setConfiguration } = useConfiguration();

  useEffect(() => {
    if (configuration && configuration.additionalNodes) {
      const rowsWithIds = configuration.additionalNodes.flatMap((node: CustomNode, nodeIndex: number) => {
        return node.fields.map((field, fieldIndex) => ({
          id: `${node.name}_${nodeIndex}_${fieldIndex}`,
          nodeName: node.name,
          fieldName: field.fieldName,
          fieldType: field.fieldType,
          csvCol: field.source?.csvCol ?? 0,
          nodeIndex,
          fieldIndex,
        }));
      });
      setRows(rowsWithIds);
    }
  }, [configuration]);

  const handleEditClick = (id: GridRowId) => () => {
    setRowModesModel((prevModel) => ({ ...prevModel, [id]: { mode: GridRowModes.Edit } }));
  };

  const handleSaveClick = (id: GridRowId) => () => {
    const updatedRow = rows.find((row) => row.id === id);
    if (updatedRow) {
      setRowModesModel((prevModel) => ({ ...prevModel, [id]: { mode: GridRowModes.View } }));
    }
  };



  const handleUpdateConfiguration = (updatedRow: RowData, rowIndex: number) => {
    const storedConfiguration = localStorage.getItem('configuration');
    const currentConfiguration = storedConfiguration ? JSON.parse(storedConfiguration) : {};
    const updatedConfiguration = getUpdatedConfiguration(updatedRow, rowIndex, currentConfiguration);
    localStorage.setItem('configuration', JSON.stringify(updatedConfiguration));
    setConfiguration(updatedConfiguration);
  };

  const getUpdatedConfiguration = (updatedRow: RowData, fieldIndex: number, currentConfig: Configuration): Configuration => {
    const nodeIndex = updatedRow.nodeIndex;
    const fieldName = toSnakeCase(updatedRow.fieldName);
    const csvCol = updatedRow.csvCol !== undefined ? updatedRow.csvCol : null;
    const nodeName = updatedRow.nodeName !== undefined ? updatedRow.nodeName : null;

    const updatedNode = { ...currentConfig.additionalNodes[nodeIndex] };
    if (nodeName !== null) {
      updatedNode.name = nodeName;
    }

    updatedNode.fields = updatedNode.fields.map((field, index) => {
      if (index === fieldIndex) {
        const updatedField = { ...field, fieldName };
        if (csvCol !== null) {
          updatedField.source = { ...field.source, csvCol };
        }
        return updatedField;
      }
      return field;
    });

    const updatedAdditionalNodes = [...currentConfig.additionalNodes];
    updatedAdditionalNodes[nodeIndex] = updatedNode;

    return {
      ...currentConfig,
      additionalNodes: updatedAdditionalNodes,
    };
  };

  const handleCancelClick = (id: GridRowId) => () => {
    setRowModesModel((prevModel) => {
      const newModel = { ...prevModel };
      delete newModel[id];
      return newModel;
    });
  };

  const processRowUpdate = (newRow: GridRowModel) => {
    const { id, ...updatedRow } = newRow;
    const updatedRows = rows.map((row) => (row.id === id ? ({ ...updatedRow, id } as RowData) : row));
    setRows(updatedRows);
    handleUpdateConfiguration(updatedRow as RowData, updatedRow.fieldIndex);

    return { ...updatedRow, id } as RowData;
  };


  const handleRowModesModelChange = (newRowModesModel: GridRowModesModel) => {
    setRowModesModel(newRowModesModel);
  };
    setRowModesModel(newRowModesModel);
  };

  const handleRowEditStop: GridEventListener<'rowEditStop'> = (params, event) => {
    if (params.reason === GridRowEditStopReasons.rowFocusOut) {
      event.defaultMuiPrevented = true;
      event.defaultMuiPrevented = true;
    }
  };

  const handleProcessRowUpdateError = (error: any) => {
    console.error('Error during row update:', error);
  };

  const columns: GridColDef[] = [
    {
      field: 'Name',
      headerName: 'List Name',
      width: 300,
      editable: true,
      align: 'left',
      headerAlign: 'left',
      valueGetter: (params) => (params.row.fieldName === 'patient' ? '' : formatNodeName(params.row.nodeName)),
    },
    {
      field: 'fieldName',
      headerName: 'Property Name',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: true,
      valueGetter: (params) => toUpperCase(params.row.fieldName),
    },
    {
      field: 'fieldType',
      headerName: 'Type',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: false,
      valueGetter: (params) => params.row.fieldType,
    },
    {
      field: 'csvCol',
      headerName: 'Csv Col',
      type: 'number',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: true,
      valueGetter: (params) => params.row.csvCol,
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: 'Actions',
      align: 'center',
      headerAlign: 'center',
      width: 300,
      cellClassName: 'actions',
      getActions: ({ id }) => {
        const isInEditMode = rowModesModel[id]?.mode === GridRowModes.Edit;
        const isInEditMode = rowModesModel[id]?.mode === GridRowModes.Edit;
        if (isInEditMode) {
          return [
            <GridActionsCellItem
              icon={<SaveIcon />}
              key="save"
              id="save-button"
              label="Save"
              sx={{ color: 'white' }}
              sx={{ color: 'white' }}
              onClick={handleSaveClick(id)}
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              key="cancel"
              id="cancel-button"
              label="Cancel"
              className="textPrimary"
              onClick={handleCancelClick(id)}
              color="inherit"
            />,
          ];
        }

        return [
          <GridActionsCellItem
            icon={<EditIcon />}
            key="edit"
            id="edit-button"
            label="Edit"
            className="textPrimary"
            onClick={handleEditClick(id)}
            color="inherit"
          />,
        ];
      },
    },
  ];

  return (
    <Box
      sx={{
        height: 500,
        width: '100%',
        '& .actions': {
          color: '#fff',
        },
        '& .textPrimary': {
          color: '#fff',
        },
      }}
    >
      {configuration && (
        <DataGrid
          rows={rows}
          columns={columns}
          editMode="row"
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          onRowEditStop={handleRowEditStop}
          processRowUpdate={processRowUpdate}
          onProcessRowUpdateError={handleProcessRowUpdateError}
          getRowId={(row) => row.id}
          slots={{
            toolbar: EditToolbar,
          }}
          slotProps={{
            toolbar: { setRows, setRowModesModel },
          }}
        />
      )}
    </Box>
  );
};

export default GoldenRecordLists;
