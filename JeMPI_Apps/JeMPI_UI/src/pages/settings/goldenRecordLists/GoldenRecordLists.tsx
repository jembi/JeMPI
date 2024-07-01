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
  GridActionsCellItem
} from '@mui/x-data-grid';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Close';
import { useEffect, useState } from 'react';
import { EditToolbar } from 'components/shared/EditToolBar';
import { formatNodeName, toSnakeCase, toUpperCase } from 'utils/helpers';
import { Configuration } from 'types/Configuration';

interface RowData {
  id: string;
  nodeName: string;
  fieldName: string;
  fieldType: string;
  csvCol: number;
}

const GoldenRecordLists = ({ goldenRecordList }: { goldenRecordList: any }) => {
  const [rows, setRows] = useState<any>([]);
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({});
  const [configuration, setConfiguration] = useState<Configuration>();

  useEffect(() => {
    if (goldenRecordList) {
      const rowsWithIds = goldenRecordList.flatMap(
        (node: { fields: any[]; nodeName: string }, nodeIndex: number) => {
          return node.fields
            ? node.fields.map((field, fieldIndex) => ({
                id: `${node.nodeName}_${nodeIndex}_${fieldIndex}`,
                nodeName: node.nodeName,
                fieldName: field.fieldName,
                fieldType: field.fieldType,
                csvCol: field.csvCol,
                nodeIndex,
                fieldIndex
              }))
            : [];
        }
      );

      setRows(rowsWithIds);
    }
  }, [goldenRecordList,configuration]);

  const handleEditClick = (id: GridRowId) => () => {
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.Edit } });
  };

  const handleSaveClick = (id: GridRowId) => () => {
    const updatedRow = rows.find((row: { id: GridRowId; }) => row.id === id);

    if (updatedRow) {
      console.log('updated row', updatedRow)
      setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.View } });
      handleUpdateConfiguration(updatedRow, updatedRow.fieldIndex);
    }
  };

  const handleUpdateConfiguration = (updatedRow: any, rowIndex: number) => {
    setConfiguration(previousConfiguration => {
      if (!previousConfiguration) {
        const initialConfig = localStorage.getItem('configuration')
        if (initialConfig) {
          previousConfiguration = JSON.parse(initialConfig)
        }
      }
      if (!previousConfiguration) {
        console.error('Configuration is not initialized')
        return previousConfiguration
      }
      const updatedConfiguration = getUpdatedConfiguration(updatedRow, rowIndex, previousConfiguration)
      localStorage.setItem('configuration', JSON.stringify(updatedConfiguration))
      return updatedConfiguration
    })
  }



  const getUpdatedConfiguration = (
    updatedRow: any,
    rowIndex: number,
    currentConfiguration: Configuration
  ): Configuration => {
    const fieldName = toSnakeCase(updatedRow.fieldName);
    console.log('field name in get configuration',updatedRow)
    const fieldToUpdate = { ...currentConfiguration.additionalNodes[rowIndex], fieldName };
    const updatedAdditionalNodes = [...currentConfiguration.additionalNodes];
    updatedAdditionalNodes[rowIndex] = fieldToUpdate;

    return {
      ...currentConfiguration,
      additionalNodes: updatedAdditionalNodes
    };
  };

  const handleCancelClick = (id: GridRowId) => () => {
    setRowModesModel({
      ...rowModesModel,
      [id]: { mode: GridRowModes.View, ignoreModifications: true }
    });
  };

  const processRowUpdate = (newRow: GridRowModel) => {
    const { id, ...updatedRow } = newRow;
    const updatedRows = rows.map((row: { id: any; }) => (row.id === id ? { ...updatedRow, id } as RowData : row));
    setRows(updatedRows);
    console.log('Row updated:', updatedRow);
    return { ...updatedRow, id } as RowData;
  };

  const handleRowModesModelChange = (newRowModesModel: GridRowModesModel) => {
    setRowModesModel(newRowModesModel);
  };

  const handleRowEditStop: GridEventListener<'rowEditStop'> = (params, event) => {
    if (params.reason === GridRowEditStopReasons.rowFocusOut) {
      event.defaultMuiPrevented = true;
    }
  };

  const columns: GridColDef[] = [
    {
      field: 'Name',
      headerName: 'List Name',
      width: 300,
      editable: true,
      align: 'left',
      headerAlign: 'left',
      valueGetter: (params) => {
        if (params.row.fieldName === 'patient') return '';
        else return formatNodeName(params.row.nodeName);
      }
    },
    {
      field: 'fieldName',
      headerName: 'Property Name',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: true,
      valueGetter: (params) => toUpperCase(params.row.fieldName)
    },
    {
      field: 'fieldType',
      headerName: 'Type',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: false,
      valueGetter: (params) => params.row.fieldType
    },
    {
      field: 'csvCol',
      headerName: 'Csv Col',
      type: 'number',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: true,
      valueGetter: (params) => params.row.csvCol
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
        if (isInEditMode) {
          return [
            <GridActionsCellItem
              icon={<SaveIcon />}
              id="save-button"
              label="Save"
              sx={{ color: 'white' }}
              onClick={handleSaveClick(id)}
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              id="cancel-button"
              label="Cancel"
              className="textPrimary"
              onClick={handleCancelClick(id)}
              color="inherit"
            />
          ];
        }

        return [
          <GridActionsCellItem
            icon={<EditIcon />}
            id="edit-button"
            label="Edit"
            className="textPrimary"
            onClick={handleEditClick(id)}
            color="inherit"
          />
        ];
      }
    }
  ];

  return (
    <Box
      sx={{
        height: 500,
        width: '100%',
        '& .actions': {
          color: '#fff'
        },
        '& .textPrimary': {
          color: '#fff'
        }
      }}
    >
      {goldenRecordList && (
        <DataGrid
          rows={rows}
          columns={columns}
          editMode="row"
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          onRowEditStop={handleRowEditStop}
          processRowUpdate={processRowUpdate}
          getRowId={(row) => row.id}
          slots={{
            toolbar: EditToolbar
          }}
          slotProps={{
            toolbar: { setRows, setRowModesModel }
          }}
        />
      )}
    </Box>
  );
};

export default GoldenRecordLists;
