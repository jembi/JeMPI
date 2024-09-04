**Section 1: Dashboard**
The Dashboard screen has 3 tabs 
- Confusion Matrix
- M & U values 
- Import Process Status.

Dashboard Tab 1: Confusion Matrix
![Dashboard](.gitbook/assets/29.png)

This tab is sub-divided into 3 sections, starting with the right, the Confusion Matrix.

**Confusion Matrix**

Understanding the confusion matrix

The confusion matrix displays a tally of the true positives, false positives, true negatives and false negatives.
This is used to calculate the precision and recall.  

The f-score is a measure of a model’s accuracy on a dataset. 
It is a harmonic mean of precision and recall. 

![Dashboard: Confusion Matrix](.gitbook/assets/30.png)

The confusion matrix provides rolling counts of the following:
![Dashboard: Confusion Matrix](.gitbook/assets/31.png)

**Beta F-scores**

![Dashboard: Beta F-scores](.gitbook/assets/37.png)

The f-score is a measure of a model’s accuracy on a dataset. It is the harmonic mean of precision and recall. There are 3 different f-scores displayed below, using the following formula:
![Dashboard: Beta F-scores](.gitbook/assets/32.png)

**Records and notifications** 

![Dashboard: Records & Notifications](.gitbook/assets/38.png)

Records 
- Displays the total number of Golden records and total number of interactions.
Notifications - Displays total number of notifications split by:

Open Notifications 
- No. of New & Open notifications
Closed notifications
 - No. of Closed notifications

Note: The number of new and open notifications (basically notifications that are not closed) affects the accuracy of the F-score and depending on the % of notifications that have not been actioned.  

**Dashboard Tab 2: M & U values**

![Dashboard: M & U values](.gitbook/assets/33.png)

This screen provides us with a view of the M & U values as per the last periodic update.  


**Tally Method**
The Tally method computes M & U’s per field which is used for cross checking against the M&U’s computed by the EM algorithm. These M& U’s are not used for probabilistic linking.  

It calculates the probabilities based on whether the fields in the pair match or do not match:
For each field where the pair matches (above notification), check if you increment A or B (refer to Tally method diagram below)
For each field where the pair do not match (below notification), check if you increment C or D
![](.gitbook/assets/34.png)

**What happens when the score is within the notification threshold area?**
When in the notification area, we are either above the notification TH or below the notification TH. 

**Above the threshold (using the incoming interaction and linked GR)**
- Assume this is correct for 80% of the time - increment A or B by 0.8
- Assume this is incorrect for 20% of the time - increment C or D by 0.2

If admin confirms this assumption, then the system must adjust the tallies by adding the 0.2 to A or B and removing 0.2 from C or D
If admin rejects this assumption, then system must subtract 0.8 from A or B and add to C or D

**Below the threshold (using the incoming interaction and the candidate GRs)**
- Assume this is correct for 80% of the time -  increment C or D by 0.8 
- Assume this is incorrect for 20% of the time - increment A or B by 0.2
If admin confirms this assumption, then the system must adjust the tallies by adding the 0.2 to C or D and removing 0.2 from A or B
If admin rejects this assumption, then system must subtract 0.8 from C or D and add to A or B

**M and U values are calculated as follows:**
- M =  A/( A+B)
- U= C/(C+D)

![Dashboard: Tally method](.gitbook/assets/35.png)

**Dashboard Tab 3: Import Process Status**

This screen displays the progress of the processing of the file uploaded via the Import screen.

 ![ Dashboard: Import Process Status](.gitbook/assets/36.png)

**Section 1: Configuration Settings**

The configuration settings screen enables the user to make edits to the default settings, 
the best fit the desired implementation of the MPI.
![Configuration Settings](.gitbook/assets/27.png)

**Common Properties**

This tab defines the demographic details for a patient that will be used for linking.
![Common Properties](.gitbook/assets/19)

**The user can do the following:**

- Select the Edit icon button to initiate edit mode on a row for the common properties.

When the row is in edit mode the following changes occur : 
- The colour of the row changes to white 
The edit icon changes to show a save icon and a close icon
![Common Properties Edit Mode](.gitbook/assets/20)

- Choose to select the close button to exit edit mode.
- Choose to select the save icon button to save changes made and exit edit mode.
- Edit the relevant fields and select the save button to save changes on the current tab.

**Deterministic**

The deterministic tab is used to define the deterministic rules.
The deterministic tab has three sub tabs : 
- Linking 
- Validate
- Matching

**Source view** 

This view allows the user to do the following :
- View the displayed rules
- Click edit mode by clicking the edit icon button which opens up the design view
- Click add icon button which initiates edit mode , switches to design view tab (If there are no existing rules on display)
![Determistic Source View](.gitbook/assets/21)

**Design view**

This view allows the user to do the following :
- Select the operator values from a drop down field eg “And” and “Or”
- Select common field values from a drop down field
- Select comparator function from a drop down field eg “Exact”, “Low Fuzziness” etc
- Add a second row of input fields by selecting the add add icon button
- Save rule by selecting the add rule button
- Exit edit mode and cancel previous edits.
- Delete existing row of input fields
![Determistic Design View](.gitbook/assets/22)

**Blocking**
The blocking tab is used to define the blocking rules.

The blocking tabs has two sub tabs : 
- Linking 
- Matching

The blocking sub tabs have two different views :
Source view This view allows the user to do the following :
- View the displayed rules
- Click edit mode by clicking the edit icon button which opens up the design view
- Click add icon button which initiates edit mode , switches to design view tab (If there are no existing rules on display)!
[Blocking Source View](.gitbook/assets/23)

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
  In the Probabilistic tab the user can define the linking threshold ranges and/or values.

  All values must be entered as per the rules defined.
  ![Probabilistic Tab](.gitbook/assets/26)

  **Rules on threshold slider**
  - Do not allow the link threshold (green circle): 
  - To be < the Minimum threshold review value
  - To be > the Maximum threshold review value

  **Rules on Threshold**
  For all threshold values that are entered, system allows for exponential notation e.g. 123E-3 which is the same as 0.012System display default values
  
  **Nodes**
  This section displays the following :
 - Golden record node
 - Interaction node
 - Source ID

  ![Nodes](.gitbook/assets/28)

  Golden record node shows properties unique to the golden record.
  Interaction node shows properties unique to the interaction.Source ID :
  The third node denoted e.g Source ID, shows unique common lists e.g 
   - Source ID list
   - Biometric ID list
