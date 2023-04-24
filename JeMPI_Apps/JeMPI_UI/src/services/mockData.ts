import AuditTrailRecord from '../types/AuditTrail'
import Notification, { NotificationState } from '../types/Notification'
import { PatientRecord } from '../types/PatientRecord'
import { User } from '../types/User'

//status
const notifications: Notification[] = [
  {
    id: '123',
    type: 'Review threshold',
    created: new Date('05-03-2023'),
    names: 'Bob Smith',
    patient_id: '0x5a',
    status: NotificationState.New,
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
    type: 'Review threshold',
    created: new Date('02-03-2023'),
    names: 'Jane Doe',
    patient_id: '0x7j',
    status: NotificationState.New,
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
    type: 'Golden record changed',
    created: new Date('02-03-2023'),
    names: 'Jane Smith',
    patient_id: '0x1a',
    status: NotificationState.New,
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
    type: 'Review threshold range',
    created: new Date('02-03-2023'),
    names: 'John Themba',
    patient_id: '0x9a',
    status: NotificationState.New,
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

const auditTrail: AuditTrailRecord[] = [
  {
    process: 'Create user',
    actionTaken: 'newUserCreated',
    links: [],
    when: '10/09/2022 8:16',
    changedBy: 'User Id 12345',
    comment: ''
  },
  {
    process: 'Auto-update golden record field',
    actionTaken: 'grUpdate',
    links: ['G543'],
    when: '11/09/2022 10:07',
    changedBy: 'System',
    comment: 'Golden ID (G543) updated field values (name, cell number)'
  },
  {
    process: 'Autolink',
    actionTaken: 'grAndPatientRecordLinked',
    links: ['G543', 'P09833'],
    when: '12/09/2022 9:00',
    changedBy: 'System',
    comment: 'Golden ID (G543) linked with Patient ID (P09833)'
  },
  {
    process: 'Approve link',
    actionTaken: 'grAndPatientRecordLinkApproved',
    links: ['G543', 'P09833'],
    when: '12/09/2022 10:35',
    changedBy: 'User ID 12345',
    comment: 'Golden ID (G543) linked with Patient ID (P09833) approved'
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
    type: 'Current',
    source: 'clinic A',
    score: 0,
    uid: '0x9832',
    nationalId: '123456',
    givenName: 'Christopher',
    familyName: 'Moltisanti',
    gender: 'Male',
    dob: new Date(1605522215666),
    phoneNumber: '085145855',
    city: 'Welkom',
    updatedBy: 'Dr Anthony Soprano',
    sourceId: {
      facility: 'Facility One',
      uid: '0x3289',
      patient: 'xxx'
    }
  },
  {
    auxId: '0x66',
    type: 'Current',
    score: 0,
    uid: '0x9833',
    nationalId: '123456',
    givenName: 'Chris',
    familyName: 'Moltisanti',
    gender: 'Male',
    dob: new Date(1605522215666),
    phoneNumber: '085145855',
    city: 'Jersey',
    updatedBy: 'Dr Carmela DeAngelis',
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
