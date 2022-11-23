import wx

API_SERVER = 'http://localhost:50000'
# LINKER_SERVICE = 'http://localhost:50010'
CONTROLLER_SERVER = 'http://localhost:50020'

REFERENCE = True


class Field:
    def __init__(self, field_name, can_edit, predicate, col_name, col_idx, align):
        self.field_name = field_name
        self.can_edit = can_edit
        self.predicate = predicate
        self.col_name = col_name
        self.col_idx = col_idx
        self.align = align


if REFERENCE:
    MY_EXTRA_FIELDS = [Field(None, False, None, 'Rec No.', 0, wx.ALIGN_CENTRE)]
    MY_ENTITY_FIELDS = \
        [Field('uid', False, None, 'MPI ID', 1, wx.ALIGN_CENTRE),
         Field('auxId', False, 'GoldenRecord.aux_id', 'AUX ID', 2, wx.ALIGN_CENTRE),
         Field('sourceId', False, 'GoldenRecord.source_id', 'SOURCE', 3, wx.ALIGN_CENTRE),
         Field('givenName', True, 'GoldenRecord.given_name', 'Given Name', 4, wx.ALIGN_LEFT),
         Field('familyName', True, 'GoldenRecord.family_name', 'Family Name', 5, wx.ALIGN_LEFT),
         Field('gender', True, 'GoldenRecord.gender', 'Gender', 6, wx.ALIGN_CENTRE),
         Field('dob', True, 'GoldenRecord.dob', 'DOB', 7, wx.ALIGN_CENTRE),
         Field('city', True, 'GoldenRecord.city', 'City', 8, wx.ALIGN_LEFT),
         Field('phoneNumber', True, 'GoldenRecord.phone_number', 'Phone Number', 9, wx.ALIGN_CENTRE),
         Field('nationalId', True, 'GoldenRecord.national_id', 'National ID', 10, wx.ALIGN_CENTRE)]
    MY_DOCUMENT_FIELDS = [
        Field('score', False, None, 'Score', 11, wx.ALIGN_RIGHT)]
else:
    MY_EXTRA_FIELDS = [Field(None, False, None, 'Rec No.', 0, wx.ALIGN_CENTRE)]
    MY_ENTITY_FIELDS = \
        [Field('uid', False, None, 'MPI ID', 1, wx.ALIGN_CENTRE),
         Field('auxId', False, 'GoldenRecord.aux_id', 'Aux ID', 2, wx.ALIGN_CENTRE),
         Field('nameGiven', True, 'GoldenRecord.name_given', 'Given', 3, wx.ALIGN_LEFT),
         Field('nameFather', True, 'GoldenRecord.name_father', "Father", 4, wx.ALIGN_LEFT),
         Field('nameFathersFather', True, 'GoldenRecord.name_fathers_father', "Father's Father", 5, wx.ALIGN_LEFT),
         Field('nameMother', True, 'GoldenRecord.name_mother', "Mother", 6, wx.ALIGN_LEFT),
         Field('nameMothersFather', True, 'GoldenRecord.name_mothers_father', "Mother's Father", 7, wx.ALIGN_LEFT),
         Field('gender', True, 'GoldenRecord.gender', 'Gender', 8, wx.ALIGN_CENTRE),
         Field('dob', True, 'GoldenRecord.dob', 'DOB', 9, wx.ALIGN_CENTRE),
         Field('city', True, 'GoldenRecord.city', 'City', 10, wx.ALIGN_LEFT),
         Field('phoneNumber', True, 'GoldenRecord.phone_number', 'Phone Number', 11, wx.ALIGN_CENTRE)]
    MY_DOCUMENT_FIELDS = [Field('score', False, None, 'Score', 12, wx.ALIGN_RIGHT)]
MY_DISPLAY_GRID_COLS = len(MY_EXTRA_FIELDS) + len(MY_ENTITY_FIELDS) + len(MY_DOCUMENT_FIELDS)
