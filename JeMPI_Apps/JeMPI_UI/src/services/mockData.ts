import { AuditTrail } from 'types/AuditTrail'
import Notification, { NotificationState } from '../types/Notification'
import { PatientRecord } from '../types/PatientRecord'
import { User } from '../types/User'

//status
const notifications: Notification[] = [
  {
    id: '123',
    type: 'THRESHOLD',
    created: new Date('05-03-2023'),
    names: 'Bob Smith',
    patient_id: '0x5a',
    status: NotificationState.CLOSED,
    golden_id: '0x9833',
    score: 0.5,
    candidates: [
      {
        golden_id: '0x45',
        score: 0.4
      }
    ]
  },
  {
    id: '345',
    type: 'THRESHOLD',
    created: new Date('02-03-2023'),
    names: 'Jane Doe',
    patient_id: '0x7j',
    status: NotificationState.OPEN,
    golden_id: '0x9824',
    score: 0.9,
    candidates: [
      {
        golden_id: '0x35',
        score: 0.4
      }
    ]
  },
  {
    id: '758',
    type: 'MARGIN',
    created: new Date('02-03-2023'),
    names: 'Jane Smith',
    patient_id: '0x1a',
    status: NotificationState.OPEN,
    golden_id: '0x9847',
    score: 0.3,
    candidates: [
      {
        golden_id: '0x45',
        score: 0.5
      }
    ]
  },
  {
    id: '541',
    type: 'UPDATE',
    created: new Date('02-03-2023'),
    names: 'John Themba',
    patient_id: '0x9a',
    status: NotificationState.OPEN,
    golden_id: '0x9866',
    score: 0.7,
    candidates: [
      {
        golden_id: '0x55',
        score: 0.7
      }
    ]
  }
]

//send notfication ID and the status ie. Done.

//TODO: update 'searchGoldenRecordResult' to match the real backend.

const searchGoldenRecordResult = {
  data: [
    {
      goldenRecord: {
        demographicData: {
          auxId: 'rec-00000000-aaa-0',
          city: 'Nairobi',
          dob: '20171114',
          familyName: 'Moltisanti',
          gender: 'male',
          givenName: 'Christopher',
          nationalId: '198804042874913',
          phoneNumber: '091-749-4674'
        },
        sourceId: [],
        uid: '0x9833'
      },
      mpiPatientRecords: []
    },
    {
      goldenRecord: {
        demographicData: {
          auxId: 'rec-00000000-aaa-0',
          city: 'Nairobi',
          dob: '20171114',
          familyName: 'Onyango',
          gender: 'male',
          givenName: 'Golden',
          nationalId: '198804042874913',
          phoneNumber: '091-749-4674'
        },
        sourceId: [],
        uid: '0x3'
      },
      mpiPatientRecords: []
    },
    {
      goldenRecord: {
        demographicData: {
          auxId: 'rec-00000000-aaa-0',
          city: 'Nairobi',
          dob: '20171114',
          familyName: 'Golden',
          gender: 'male',
          givenName: 'Endalekachew',
          nationalId: '198804042874913',
          phoneNumber: '091-749-4674'
        },
        sourceId: [],
        uid: '0x2'
      },
      mpiPatientRecords: []
    }
  ],
  pagination: {
    total: 100
  }
}

const searchPatientRecordResult = {
  data: [
    {
      demographicData: {
        auxId: 'rec-00000000-aaa-0',
        city: 'Nairobi',
        dob: '20171114',
        familyName: 'Patient',
        gender: 'male',
        givenName: 'Endalekachew',
        nationalId: '198804042874913',
        phoneNumber: '091-749-4674'
      },
      uid: '0x5a'
    },
    {
      demographicData: {
        auxId: 'rec-00000000-bbb-0',
        city: 'Nairobi',
        dob: '20171114',
        familyName: 'Onyango',
        gender: 'male',
        givenName: 'Patient',
        nationalId: '198804042874913',
        phoneNumber: '091-749-4674'
      },
      uid: '0x8'
    },
    {
      demographicData: {
        auxId: 'rec-00000000-ccc-0',
        city: 'Nairobi',
        dob: '20171114',
        familyName: 'Patient',
        gender: 'male',
        givenName: 'Endalekachew',
        nationalId: '198804042874913',
        phoneNumber: '091-749-4674'
      },
      uid: '0x7'
    }
  ],
  pagination: {
    total: 100
  }
}

const auditTrail: AuditTrail[] = [
  {
    inserted_at: '2023-09-05 14:46:51.000991',
    created_at: '2023-09-05 14:46:51.000988',
    interaction_id: '0x1627',
    golden_id: '0x1628',
    entry: 'Interaction -> New GoldenRecord (1.000000)'
  },
  {
    inserted_at: '2023-09-05 14:46:51.000991',
    created_at: '2023-09-05 14:46:51.000988',
    interaction_id: '0x1627',
    golden_id: '0x1628',
    entry: 'Interaction -> New GoldenRecord (1.000000)'
  },
  {
    inserted_at: '2023-09-05 14:46:51.000991',
    created_at: '2023-09-05 14:46:51.000988',
    interaction_id: '0x1627',
    golden_id: '0x1628',
    entry: 'Interaction -> New GoldenRecord (1.000000)'
  }
]

const patientRecord = {
  demographicData: {
    auxId: '0x66',
    nationalId: '123456',
    givenName: 'Bob',
    familyName: 'Smith',
    gender: 'Male',
    dob: new Date(1605522215666),
    phoneNumber: '085145855',
    city: 'Welkom'
  },
  facilityId: 'Lab01',
  patientId: 'P567',
  updated: '1234',
  uid: '0x5a',
  sourceId: {
    facility: 'Facility One',
    uid: '0x3289',
    patient: 'xxx'
  }
}

const linkedRecords: PatientRecord[] = [
  {
    auxId: '0x66',
    score: 0,
    uid: '0x9832',
    demographicData: {
      nationalId: '123456',
      givenName: 'Christopher',
      familyName: 'Moltisanti',
      gender: 'Male',
      dob: new Date(1605522215666).toISOString(),
      phoneNumber: '085145855',
      city: 'Welkom'
    },
    sourceId: {
      facility: 'Facility One',
      uid: '0x3289',
      patient: 'xxx'
    },
    createdAt: new Date(1605522215666).toISOString()
  },
  {
    auxId: '0x66',
    score: 0,
    uid: '0x9833',
    demographicData: {
      nationalId: '123456',
      givenName: 'Chris',
      familyName: 'Moltisanti',
      gender: 'Male',
      dob: new Date(1605522215666).toISOString(),
      phoneNumber: '085145855',
      city: 'Jersey'
    },
    createdAt: new Date(1605522215666).toISOString(),
    sourceId: {
      facility: 'Facility Two',
      uid: '0x3288',
      patient: 'xxx'
    }
  }
]

const goldenRecord = {
  goldenRecord: {
    uid: '0x45',
    demographicData: {
      auxId: '0x77',
      nationalId: '123456',
      givenName: 'Bob',
      familyName: 'Smith',
      gender: 'Male',
      dob: new Date(1605522215666),
      phoneNumber: '085145855',
      city: 'Welkom'
    },
    facilityId: 'Lab01',
    patientId: 'P567',
    updated: '1234',
    sourceId: [
      {
        facility: 'Facility One',
        uid: '0x3289',
        patient: 'xxx'
      },
      {
        facility: 'Facility Two',
        uid: '0x3288',
        patient: 'xxx'
      }
    ]
  },
  mpiPatientRecords: [
    { patientRecord: linkedRecords[0] },
    { patientRecord: linkedRecords[1] }
  ]
}

const patientRecords = [patientRecord, goldenRecord]

const goldenRecords = [
  {
    ...goldenRecord
  }
]

const currentUser: User = {
  id: 1,
  username: 'john',
  email: 'john.doe@jembi.org',
  familyName: 'John',
  givenName: 'Doe',
  provider: 'keycloak'
}

const mockData = {
  auditTrail,
  notifications,
  patientRecord,
  goldenRecord,
  goldenRecords,
  patientRecords,
  currentUser,
  linkedRecords,
  searchGoldenRecordResult,
  searchPatientRecordResult
}

export default mockData
