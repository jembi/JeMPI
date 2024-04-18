import Draggable from "react-draggable";
import React, { useState, useRef } from "react";
import '../Shapes.css';

const GoldenRecordInteractiveNode  = () => {
 
      const [currentRotate, setCurrentRotate] = useState(0);
    
      const isDraggingRef = useRef(false);
    
      const onDrag = () => {
        isDraggingRef.current = true;
      };
    
      const onStop = () => {
        if (!isDraggingRef.current) {
          setCurrentRotate(currentRotate + 90);
        }
        isDraggingRef.current = false;
      };
    
      return (
        <Draggable onStop={onStop} onDrag={onDrag}>
      <div className="circle-container">
        <div
          className="circle"
          style={{ transform: "rotate(" + currentRotate + "deg)" }}
        >
          <span className="label">Golden Record</span>
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
        </div>
      </Draggable>
      );
    };
    
export default GoldenRecordInteractiveNode;
