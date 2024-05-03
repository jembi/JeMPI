import { Grid } from '@mui/material'
import { useState, useEffect } from 'react'
import '../Shapes.css'
import Draggable from 'react-draggable'

declare global {
  interface Window {
    LeaderLine: any
  }
}

const InteractiveNode = () => {
  const [line1, setLine1] = useState<any>(null)
  const [line2, setLine2] = useState<any>(null)
  const [line3, setLine3] = useState<any>(null)

  useEffect(() => {
    if (window.LeaderLine) {
      const newLine1 = new window.LeaderLine(
        document.getElementById('start1'),
        document.getElementById('end1'),
        {
          color: 'gray',
          startPlug: 'behind',
          endPlug: 'behind',
          path: 'straight'
        }
      )

      const newLine2 = new window.LeaderLine(
        document.getElementById('end1'),
        document.getElementById('start2'),
        {
          color: 'gray',
          startPlug: 'behind',
          endPlug: 'behind',
          path: 'straight'
        }
      )

      const newLine3 = new window.LeaderLine(
        document.getElementById('start1'),
        document.getElementById('start2'),
        {
          color: 'gray',
          path: 'straight',
          startPlug: 'behind',
          endPlug: 'behind',
          dash: true
        }
      )

      setLine1(newLine1)
      setLine2(newLine2)
      setLine3(newLine3)
    } else {
      console.error('LeaderLine library not loaded!')
    }
  }, [])

  const handleDrag = () => {
    if (line1) line1.position()
    if (line2) line2.position()
    if (line3) line3.position()
  }

  return (
    <Grid container spacing={4}>
      <Grid item md={4}>
        <div
          style={{
            display: 'flex',
            flexDirection: 'row',
            justifyContent: 'center',
            alignItems: 'center',
            justifyItems: 'center',
            alignContent: 'center',
            gap: 0,
            width: '100%'
          }}
        >
          <div className="shapes-container">
            <Draggable
              handle=".handle"
              onDrag={handleDrag}
              grid={[1, 1]}
              scale={1}
              cancel=".cancel"
            >
              <div
                id="start1"
                className="circle handle circle-container"
                style={{ backgroundColor: '#ADD8E6', marginBottom: '300px' }}
              >
                <b className="label">Golden Record</b>
                <div className="additional-info">
                  <h2>Common Properties</h2>
                  <ul>
                    <li>Family Name: Smith</li>
                    <li>Name: John</li>
                    <li>City: New York</li>
                    <li>Age: 30</li>
                    <li>Phone Number: 123-456-7890</li>
                    <li>National ID: 123456789</li>
                  </ul>
                </div>
              </div>
            </Draggable>
            <Draggable
              handle=".handle"
              onDrag={handleDrag}
              grid={[1, 1]}
              scale={1}
              cancel=".cancel"
            >
              <div id="start2" className="circle handle ">
                <b className="label">
                  Interaction
                  <br />
                  (encounter)
                </b>
              </div>
            </Draggable>
          </div>
          <div className="shapes-container">
            <div id="end1" style={{ left: 100 }} className="square">
              <span className="label"></span>
            </div>
          </div>
        </div>
      </Grid>
    </Grid>
  )
}
export default InteractiveNode