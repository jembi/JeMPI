**Configuration Settings**
The configuration settings screen enables the user to make edits to the default settings, the best fit the desired implementation of the MPI.

![Configuration Settings](.gitbook/assets/27.png)

**Common Properties**
This tab defines the demographic details for a patient that will be used for linking.
![Common Properties](.gitbook/assets/19)

**The user can do the following:**
- Select the Edit icon button to initiate edit mode on a row for the common properties.
When the row is in edit mode the following changes occur :
The colour of the row changes to white 
The edit icon changes to show a save icon and a close icon
![Common Properties Edit Mode](.gitbook/assets/20)

-Choose to select the close button to exit edit mode.
-Choose to select the save icon button to save changes made and exit edit mode.
-Edit the relevant fields and select the save button to save changes on the current tab.

**Deterministic**
The deterministic tab  is used to define the deterministic rules. 

The deterministic tab has three sub tabs : 
- Linking
- Validate 
- Matching 

**Source view**
 this view allows the user to do the following :
- View the displayed rules
- Click edit mode by clicking the edit icon button which opens up the design view
- Click add icon button which initiates edit mode ,  switches to design - - View tab (If there are no existing rules on display)
![Determistic Source View](.gitbook/assets/21)

**Design view**
this view allows the user to do the following :
- Select the operator values from a drop down field eg “And” and “Or”
- Select common field values from a drop down field
- Select comparator function from a drop down field eg “Exact”, “Low Fuzziness” etc
- Add a second row of input fields by selecting the add add icon button
- Save rule by selecting the add rule button
- Exit edit mode and cancel previous edits.
- Delete existing row of input fields
![Determistic Design View](.gitbook/assets/22)

**Blocking**
The blocking tab  is used to define the blocking rules. 

The blocking tabs has two sub tabs : 
- Linking 
- Matching 

The blocking sub tabs  have two different views :

Source view 
This view allows the user to do the following :
- View the displayed rules
- Click edit mode by clicking the edit icon button which opens up the design view
- Click add icon button which initiates edit mode ,  switches to design view tab (If there are no existing rules on display)
![Blocking Source View](.gitbook/assets/23)

**Design view**
This view must allows the user to do the following :

- Select the operator values from a drop down field eg “And” and “Or”
- Select common field values from a drop down field.
- Select comparator function from a drop down field eg “Exact”, “Low Fuzziness” etc
- Add a second row of input fields by selecting the add add icon button
- Save rule by selecting the add rule button
- Exit edit mode and cancel previous edits.
- Delete existing row of input fields
![Blocking Design View](.gitbook/assets/25)

**Probabilistic**
   In the Probabilistic tab the user can  define the linking threshold ranges and/or values. 

All values must be entered as per the rules defined.
![Probabilistic Tab](.gitbook/assets/26)

**Rules on threshold slider**
- Do not allow the link threshold (green circle):
To be < the Minimum threshold review value
To be > the Maximum threshold review value

**Rules on Threshold**
For all threshold values that are entered, system allows for exponential notation e.g. 123E-3 which is the same as 0.012
System display default values 

**Nodes**
This section displays the following  : 
- Golden record node
- Interaction node
- Source ID

![Nodes](.gitbook/assets/28)

Golden record node shows properties unique to the golden record.
Interaction node shows properties unique to the interaction.
Source ID : The third node denoted e.g Source ID, shows unique common lists e.g 
- Source ID list
- Biometric ID list
