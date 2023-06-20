# basefunctions.py - Python module that provides basic functionality needed by
#                    other modules of the flexible data generation system.
#
# Peter Christen and Dinusha Vatsalan, January-March 2012
# Modified by Sepideh Mosaferi 05/31/2017
# =============================================================================
#
#  This Source Code Form is subject to the terms of the Mozilla Public
#  License, v. 2.0. If a copy of the MPL was not distributed with this
#  file, You can obtain one at http://mozilla.org/MPL/2.0/.
#
# =============================================================================

import codecs  # Used to read and write Unicode files
import os
import types

# -----------------------------------------------------------------------------
# Functions to check type and range of variables, used to check user parameters


def check_is_not_none(variable, value):
    """Check if the value given is not None.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if value is None:
        raise Exception('Value of "%s" is None' % (variable))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_string(variable, value):
    """Check if the value given is of type string.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if not isinstance(value, str):
        raise Exception('Value of "%s" is not a string: %s (%s)' %
                        (variable, str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_unicode_string(variable, value):
    """Check if the value given is of type unicode string.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if not isinstance(value, str):
        raise Exception('Value of "%s" is not a Unicode string: %s (%s)' %
                        (variable, str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_string_or_unicode_string(variable, value):
    """Check if the value given is of type string or unicode string.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if (not isinstance(value, str)) and (not isinstance(value, str)):
        raise Exception('Value of "%s" is neither a string nor a Unicode ' %
                        (variable) + 'string: %s (%s)' % (str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_non_empty_string(variable, value):
    """Check if the value given is of type string and is not an empty string.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    if (not isinstance(variable, str)) or (variable == ''):
        raise Exception('Value of "variable" is not a non-empty string: %s (%s)'
                        % (str(variable), type(variable)))

    if (not isinstance(value, str)) or (value == ''):
        raise Exception('Value of "%s" is not a non-empty string: %s (%s)' %
                        (variable, str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_number(variable, value):
    """Check if the value given is a number, i.e. of type integer or float.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if ((not isinstance(value, int)) and (not isinstance(value, float))):
        raise Exception('Value of "%s" is not a number: %s (%s)' %
                        (variable, str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_positive(variable, value):
    """Check if the value given is a positive number, i.e. of type integer or
       float, and larger than zero.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if ((not isinstance(value, int)) and (not isinstance(value, float)) or
            (value <= 0.0)):
        raise Exception('Value of "%s" is not a positive number: ' %
                        (variable) + '%s (%s)' % (str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_not_negative(variable, value):
    """Check if the value given is a non-negative number, i.e. of type integer or
       float, and larger than or equal to zero.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if ((not isinstance(value, int)) and (not isinstance(value, float)) or
            (value < 0.0)):
        raise Exception('Value of "%s" is not a number or it is a ' %
                        (variable) + 'negative number: %s (%s)' %
                        (str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_normalised(variable, value):
    """Check if the value given is a number, i.e. of type integer or float, and
       between (including) 0.0 and 1.0.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if ((not isinstance(value, int)) and (not isinstance(value, float)) or
            (value < 0.0) or (value > 1.0)):
        raise Exception('Value of "%s" is not a normalised number ' %
                        (variable) + '(between 0.0 and 1.0): %s (%s)' %
                        (str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_percentage(variable, value):
    """Check if the value given is a number, i.e. of type integer or float, and
       between (including) 0 and 100.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if ((not isinstance(value, int)) and (not isinstance(value, float)) or
            (value < 0.0) or (value > 100.0)):
        raise Exception('Value of "%s" is not a percentage number ' %
                        (variable) + '(between 0.0 and 100.0): %s (%s)' %
                        (str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_integer(variable, value):
    """Check if the value given is an integer number.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if not isinstance(value, int):
        raise Exception('Value of "%s" is not an integer: %s (%s)' %
                        (variable, str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_float(variable, value):
    """Check if the value given is a floating-point number.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if not isinstance(value, float):
        raise Exception('Value of "%s" is not a floating point ' % (variable) +
                        'number: %s (%s)' % (str(value), type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_dictionary(variable, value):
    """Check if the value given is of type dictionary.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if not isinstance(value, dict):
        raise Exception('Value of "%s" is not a dictionary: %s' %
                        (variable, type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_list(variable, value):
    """Check if the value given is of type dictionary.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if not isinstance(value, list):
        raise Exception('Value of "%s" is not a list: %s' %
                        (variable, type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_set(variable, value):
    """Check if the value given is of type set.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if not isinstance(value, set):
        raise Exception('Value of "%s" is not a set: %s' %
                        (variable, type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_tuple(variable, value):
    """Check if the value given is of type tuple.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if not isinstance(value, tuple):
        raise Exception('Value of "%s" is not a tuple: %s' %
                        (variable, type(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_flag(variable, value):
    """Check if the value given is either True or False.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if value not in [True, False]:
        raise Exception('Value of "%s" is not True or False: %s' % (variable, str(value)))

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


def check_is_function_or_method(variable, value):
    """Check if the value given is a function or method.

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if type(value) not in [types.FunctionType, types.MethodType]:
        raise Exception('%s is not a function or method: %s' %
                        (str(variable), type(value)))

# -----------------------------------------------------------------------------


def check_unicode_encoding_exists(unicode_encoding_str):
    """A function which checks if the given Unicode encoding string is known to
       the Python codec registry.

       If the string is unknown this functions ends with an exception.
    """

    check_is_string_or_unicode_string('unicode_encoding_str',
                                      unicode_encoding_str)

    try:
        codecs.lookup(unicode_encoding_str)
    except:
        raise Exception('Unknown Unicode encoding string: "%s"' %
                        (unicode_encoding_str))

# -----------------------------------------------------------------------------


def char_set_ascii(s):
    """Determine if the input string contains digits, letters, or both, as well
       as whitespaces or not.

       Returns a string containing the set of corresponding characters.
    """

    check_is_string_or_unicode_string('s', s)

    if len(s) == 0:
        return ''

    if ' ' in s:
        includes_spaces = True
    else:
        includes_spaces = False

    # Remove whitespaces
    #
    check_str = s.replace(' ', '')

    # Check if string contains characters other than alpha-numeric characters
    #
    if not check_str.isalnum():
        return ''

        # Return an empty string rather than stopping program
        #
        # raise Exception, 'The string "%s" contains characters other than ' % \
        #                 (check_str) + 'alpha numeric and whitespace'

    # Check if string contains letters only, digits only, or both
    #
    if check_str.isdigit():
        char_set = '0123456789'
    elif check_str.isalpha():
        char_set = 'abcdefghijklmnopqrstuvwxyz'
    else:
        char_set = 'abcdefghijklmnopqrstuvwxyz0123456789'

    if includes_spaces:
        char_set += ' '

    return char_set

# -----------------------------------------------------------------------------


def check_is_valid_format_str(variable, value):
    """Check if the value given is a valid formatting string for numbers.
       Possible formatting values are:

       int, float1, float2, float3, float4, float5, float6, float7, float8, or
       float9

       The argument 'variable' needs to be set to the name (as a string) of the
       value which is checked.
    """

    check_is_non_empty_string('variable', variable)

    if (value not in ['int', 'float1', 'float2', 'float3', 'float4', 'float5',
                      'float6', 'float7', 'float8', 'float9']):
        raise Exception('%s is not a validformat string: %s' %
                        (str(variable), type(value)))

# -----------------------------------------------------------------------------


def float_to_str(f, format_str):
    """Convert the given floating-point (or integer) number into a string
       according to the format string given.

       The format string can be one of 'int' (return a string that corresponds to
       an integer value), or 'float1', 'float2', ..., 'float9' which returns a
       string of the number with the specified number of digits behind the comma.
    """

    check_is_number('f', f)

    check_is_string('format_str', format_str)
    check_is_valid_format_str('format_str', format_str)

    if format_str == 'int':
        f_str = '%.0f' % f
    elif format_str == 'float1':
        f_str = '%.1f' % f
    elif format_str == 'float2':
        f_str = '%.2f' % f
    elif format_str == 'float3':
        f_str = '%.3f' % f
    elif format_str == 'float4':
        f_str = '%.4f' % f
    elif format_str == 'float5':
        f_str = '%.5f' % f
    elif format_str == 'float6':
        f_str = '%.6f' % f
    elif format_str == 'float7':
        f_str = '%.7f' % f
    elif format_str == 'float8':
        f_str = '%.8f' % f
    elif format_str == 'float9':
        f_str = '%.9f' % f
    else:
        raise Exception('Illegal string format given: "%s"' % (format_str))

    return f_str

# -----------------------------------------------------------------------------


def str2comma_separated_list(s):
    """A function which splits the values in a list at commas, and checks all
       values if they are quoted (double or single) at both ends or not. Quotes
       are removed.

       Note that this function will split values that are quoted but contain one
       or more commas into several values.
    """

    check_is_unicode_string('s', s)

    in_list = s.split(',')
    out_list = []

    for e in in_list:
        e = e.strip()
        if ((e.startswith('"') and e.endswith('"')) or
                (e.startswith("'") and e.endswith("'"))):
            e = e[1:-1]  # Remove quotes
        out_list.append(e)

    return out_list

# -----------------------------------------------------------------------------


def read_csv_file(file_name, encoding, header_line):
    """Read a comma separated values (CSV) file from disk using the given Unicode
       encoding.

       Arguments:
       file_name    Name of the file to read.

       encoding     The name of a Unicode encoding to be used when reading the
                    file.
                    If set to None then the standard 'ascii' encoding will be
                    used.

       header_line  A flag, set to True or False, that has to be set according
                    to if the frequency file starts with a header line or not.

       This function returns two items:
       - If given, a list that contains the values in the header line of the
         file. If no header line was given, this item will be set to None.

       - A list containing the records in the CSV file, each as a list.

       Notes:
       - Lines starting with # are assumed to contain comments and will be
         skipped. Lines that are empty will also be skipped.
       - The CSV files must not contain commas in the values, while values
         in quotes (double or single) can be handled.
    """

    check_is_string('file_name', file_name)
    check_is_flag('header_line', header_line)

    if encoding is None:  # Use default ASCII encoding
        encoding = 'ascii'
    check_is_string('encoding', encoding)
    check_unicode_encoding_exists(encoding)

    try:
        in_file = codecs.open(file_name, encoding=encoding)
    except:
        raise IOError('Cannot read CSV file "%s"' % (file_name))

    if header_line:
        header_line = in_file.readline()
        # print 'Header line:', header_line

        header_list = str2comma_separated_list(header_line)

    else:
        # print 'No header line'
        header_list = None

    file_data = []

    for line_str in in_file:
        line_str = line_str.strip()
        if (line_str.startswith('#') is False) and (line_str != ''):
            line_list = str2comma_separated_list(line_str)

            file_data.append(line_list)

    in_file.close()

    return header_list, file_data

# -----------------------------------------------------------------------------


def write_csv_file(file_name, encoding, header_list, file_data):
    """Write a comma separated values (CSV) file to disk using the given Unicode
       encoding.

       Arguments:
       file_name    Name of the file to write.

       encoding     The name of a Unicode encoding to be used when reading the
                    file.
                    If set to None then the standard 'ascii' encoding will be
                    used.

       header_list  A list containing the attribute (field) names to be written
                    at the beginning of the file.
                    If no header line is to be written then this argument needs
                    to be set to None.

       file_data    A list containing the records to be written into the CSV
                    file. Each record must be a list of values, and these values
                    will be concatenated with commas and written into the file.
                    It is assumed the values given do not contain comas.
    """

    check_is_string('file_name', file_name)
    check_is_list('file_data', file_data)

    if encoding is None:  # Use default ASCII encoding
        encoding = 'ascii'
    check_is_string('encoding', encoding)
    check_unicode_encoding_exists(encoding)

    try:
        out_file = codecs.open(file_name, 'w', encoding=encoding)
    except:
        raise IOError('Cannot write CSV file "%s"' % (file_name))

    if header_list is not None:
        check_is_list('header_list', header_list)
        header_str = ','.join(header_list)
        # print 'Header line:', header_str
        out_file.write(header_str+os.linesep)

    i = 0
    for rec_list in file_data:
        check_is_list('rec_list %d' % (i), rec_list)

        line_str = ','.join(rec_list)
        out_file.write(line_str+os.linesep)

        i += 1

    out_file.close()

# -----------------------------------------------------------------------------
