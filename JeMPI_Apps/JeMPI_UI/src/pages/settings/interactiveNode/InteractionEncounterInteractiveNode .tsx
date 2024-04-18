import Draggable from "react-draggable";
import React, { useState, useRef } from "react";
import '../Shapes.css';

const InteractionEncounterInteractiveNode   = () => {
 
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
          <div>
            <div
              className="interaction-circle"
              style={{ transform: "rotate(" + currentRotate + "deg)" }}
            >
              <span className="label">Interaction</span>
            </div>
          </div>
        </Draggable>
      );
    };
    
export default InteractionEncounterInteractiveNode ;

