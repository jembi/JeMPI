import { Box } from '@mui/material'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Paper from '@mui/material/Paper'

function createData(fieldName: string, mValue: number, uValue: number) {
  return { fieldName, mValue, uValue }
}
const rows = [
  createData('Given Name', 75, 6.0),
  createData('Family Name', 237, 9.0),
  createData('Gender', 0.5, 0.7),
  createData('Birthdate', 305, 3.7),
  createData('Phone', 305, 3.7),
  createData('National Id ', 305, 3.7),
  createData('City ', 305, 3.7)
]
function MandU({data, ...rest}: any) {

   
  return (
    <Box component={'fieldset'}>
      <legend>M & U Values</legend>
      <TableContainer component={Paper} sx={{ minHeight: '450px' }}>
        <Table aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>Field Name</TableCell>
              <TableCell align="center">M value</TableCell>
              <TableCell align="center">U value</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data && Object.entries(data).map(([fieldId, m_and_u]) => (
              <TableRow key={fieldId}>
                <TableCell component="th" scope="row">
                  {fieldId}
                </TableCell>
                <TableCell align="center" scope="row">
                  {parseFloat((m_and_u as any).m).toFixed(7)}
                </TableCell>
                <TableCell align="center">{parseFloat((m_and_u as any).u).toFixed(7)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  )
}

export default MandU
