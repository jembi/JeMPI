import { useEffect, useState } from 'react';
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
import { EditToolbar } from 'components/shared/EditToolBar';
import { processIndex, toSnakeCase, transformFieldName } from 'utils/helpers';
import { useConfiguration } from 'hooks/useUIConfiguration';
import { Configuration, Field, LinkMetaData } from 'types/Configuration';
import { IconButton, Tooltip } from '@mui/material';
import { RowData } from '../deterministic/SourceView';
import { useSnackbar } from 'notistack';
import FieldDialog from './FieldDialog';
import AddIcon from '@mui/icons-material/Add'

const CommonSettings = () => {
  const [rows, setRows] = useState<any>([]);
  const { configuration, setConfiguration } = useConfiguration();
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({});
  const [openFieldModal, setOpenFieldModal] = useState<boolean>(false);
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    if (configuration && configuration.demographicFields) {
      const rowData = configuration.demographicFields.map(
        (row: any, rowIndex: number) => ({
          id: rowIndex + 1,
          ...row,
          rowIndex
        })
      );
      setRows(rowData);
    }
  }, [configuration]);

  const handleEditClick = (id: GridRowId) => () => {
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.Edit } });
  };

  const handleSaveClick = (id: GridRowId) => () => {
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.View } });
  };

  const handleUpdateConfiguration = (updatedRow: any, rowIndex: number) => {
    if (!configuration) return;
    const updatedConfiguration = getUpdatedConfiguration(
      updatedRow,
      rowIndex,
      configuration
    );
    localStorage.setItem('configuration', JSON.stringify(updatedConfiguration));
    setConfiguration(updatedConfiguration);
    setRows((prevRows: any) =>
      prevRows.map((row: any) =>
        row.id === updatedRow.id ? { ...updatedRow } : row
      )
    );
  };

  const getUpdatedConfiguration = (
    updatedRow: any,
    rowIndex: number,
    currentConfiguration: Configuration
  ): Configuration => {
    const newConfiguration = { ...currentConfiguration };
    const fieldName = toSnakeCase(updatedRow.fieldName);
    if (!newConfiguration.demographicFields) {
      return currentConfiguration;
    }

    const fieldToUpdate = { ...newConfiguration.demographicFields[rowIndex] };

    if (!fieldToUpdate) {
      return currentConfiguration;
    }

    fieldToUpdate.fieldName = fieldName;

    if (updatedRow?.indexGoldenRecord) {
      fieldToUpdate.indexGoldenRecord = `@index(${updatedRow.indexGoldenRecord.replace(
        ' ',
        ''
      )})`;
    }

    if (updatedRow?.m) {
      fieldToUpdate.linkMetaData = {
        ...fieldToUpdate.linkMetaData,
        m: Number(updatedRow.m)
      } as LinkMetaData;
    }

    if (updatedRow?.u) {
      fieldToUpdate.linkMetaData = {
        ...fieldToUpdate.linkMetaData,
        u: Number(updatedRow.u)
      } as LinkMetaData;
    }

    newConfiguration.demographicFields[rowIndex] = fieldToUpdate;

    return newConfiguration;
  };

  const handleCancelClick = (id: GridRowId) => () => {
    setRowModesModel(prevRowModesModel => {
      const newRowModesModel = { ...prevRowModesModel };
      delete newRowModesModel[id];
      return newRowModesModel;
    });
  };

  const handleRowEditStop: GridEventListener<'rowEditStop'> = ({ reason }) =>
    reason === GridRowEditStopReasons.rowFocusOut;

  const handleRowModesModelChange = (newRowModesModel: GridRowModesModel) => {
    setRowModesModel(newRowModesModel);
  };

  const processRowUpdate = (newRow: GridRowModel, oldRow: GridRowModel) => {
    const isFieldNameValid = newRow?.fieldName && newRow.fieldName.toLowerCase() !== 'unknown_field';
  
    if (!isFieldNameValid) {
      enqueueSnackbar('Field name cannot be empty or unknown', { variant: 'error' });
    }
  
    const { id, ...updatedRow } = newRow;
    const updatedRows = rows.map((row: { id: any }) =>
      row.id === id ? ({ ...updatedRow, id } as RowData) : row
    );
  
    setRows(updatedRows);
    handleUpdateConfiguration(updatedRow, updatedRow.rowIndex);
    return { ...updatedRow, id } as RowData;
  };

  const handleAddNewRow = (fieldName: string) => {
    const newRow: Field = {
      id: (rows.length + 1).toString(),
      fieldName,
      fieldType: 'String',
      linkMetaData: {
        comparison: '',
        comparisonLevels: [],
        m: 0,
        u: 0
      },
    };

    if (configuration) {
      const newConfiguration = {
        ...configuration,
        demographicFields: [...configuration.demographicFields, newRow]
      };

      localStorage.setItem('configuration', JSON.stringify(newConfiguration));
      setConfiguration(newConfiguration);
      setRows((prevRows: any) => [...prevRows, newRow]);
      setRowModesModel(prevRowModesModel => ({
        ...prevRowModesModel,
        [(newRow.id) as string]: { mode: GridRowModes.Edit, fieldToFocus: 'fieldName' }
      }));
    } else {
      console.error("Configuration is null. Cannot add new row.");
    }
  };

  const columns: GridColDef[] = [
    {
      field: 'fieldName',
      headerName: 'Name',
      width: 180,
      editable: true,
      align: 'left',
      headerAlign: 'left',
      valueGetter: params => transformFieldName(params)
    },
    {
      field: 'fieldType',
      headerName: 'Type',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: true,
    },
    {
      field: 'indexGoldenRecord',
      headerName: 'Index',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: true,
      valueGetter: params => {
        const indexGoldenRecord = params.row.indexGoldenRecord;
        return processIndex(indexGoldenRecord);
      }
    },
    {
      field: 'm',
      headerName: 'Default M',
      width: 180,
      editable: true,
      type: 'number',
      align: 'center',
      headerAlign: 'center',
      valueGetter: params => {
        const linkMetaData = params.row.linkMetaData;
        if (linkMetaData && typeof linkMetaData.m === 'number') {
          return linkMetaData.m.toFixed(7);
        }
        return;
      }
    },
    {
      field: 'u',
      headerName: 'Default U',
      width: 180,
      editable: true,
      type: 'number',
      align: 'center',
      headerAlign: 'center',
      valueGetter: params => {
        const linkMetaData = params.row.linkMetaData;
        if (linkMetaData && typeof linkMetaData.u === 'number') {
          return linkMetaData.u.toFixed(7);
        }
      }
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
        const row = rows.find((row: any) => row.id === id);
        const isFieldNameValid = row?.fieldName && row.fieldName.toLowerCase() !== 'unknown_field';
      
        if (isInEditMode) {
          return [
            <GridActionsCellItem
              icon={<SaveIcon />}
              id="save-button"
              label="Save"
              sx={{ color: 'white' }}
              onClick={handleSaveClick(id)}
              disabled={!isFieldNameValid}
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              id="cancel-button"
              label="Cancel"
              className="textPrimary"
              onClick={handleCancelClick(id)}
              color="inherit"
              disabled={!isFieldNameValid}
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
        width: '100%'
      }}
    >
      <Box sx={{display:'flex', justifyContent:'flex-end', marginBottom:'20px', marginX:'135px'}}>
      <Tooltip title="Add a new field" arrow>
      <IconButton key="add-new-field" size='small' onClick={() => setOpenFieldModal(true)}>
            <AddIcon />
       </IconButton>
       </Tooltip>
      </Box>
      <FieldDialog 
        openFieldModal={openFieldModal} 
        setOpenFieldModal={setOpenFieldModal} 
        onSave={handleAddNewRow}
      />
      {configuration && (
        <DataGrid
          rows={rows}
          columns={columns}
          editMode="row"
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          processRowUpdate={processRowUpdate}
          onRowEditStop={handleRowEditStop}
          onProcessRowUpdateError={(error) => {
            console.error(error);
          }}
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

export default CommonSettings;
