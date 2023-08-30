import numpy as np
import pandas as pd
import re
from collections.abc import Generator


def __find_ceil(df, val, lo, hi):
    while lo < hi:
        mid = lo + ((hi - lo) >> 1)  # Same as mid = (l+h)/2
        if val > df.at[mid, 'prefix']:
            lo = mid + 1
        else:
            hi = mid

    if df.at[lo, 'prefix'] >= val:
        return lo
    else:
        return -1


def name_generator(seed, csv_filename):
    """
    Random name generator

    Args:
        seed: random number generator's seed.
        csv_filename: filename of the frequency table. The csv file must have the following columns [name, frequency]

    Returns:
        yields a random name
    """
    rng = np.random.default_rng(seed)
    freq_table = pd.read_csv(csv_filename, header=None)
    freq_table.columns = ['name', 'freq']
    freq_table['prefix'] = freq_table['freq'].cumsum(axis=0)
    high = freq_table.last_valid_index()
    prefix_high = freq_table.at[high, 'prefix']
    while True:
        r = rng.integers(0, prefix_high + 1)
        idx = __find_ceil(freq_table, r, 0, high)
        yield idx, freq_table.at[idx, 'name'].lower()


def city_generator(seed, csv_filename):
    """
    Random town & region generator

    Args:
        seed: random number generator's seed.
        csv_filename: filename of the frequency table. The csv file must have the following columns [town, frequency, region]

    Returns:
        yields a random town & region
    """
    rng = np.random.default_rng(seed)
    freq_table = pd.read_csv(csv_filename, header=None)
    freq_table.columns = ['city', 'freq']
    freq_table['prefix'] = freq_table['freq'].cumsum(axis=0)
    high = freq_table.last_valid_index()
    prefix_high = freq_table.at[high, 'prefix']
    while True:
        r = rng.integers(0, prefix_high + 1)
        idx = __find_ceil(freq_table, r, 0, high)
        yield idx, freq_table.at[idx, 'city']


def date_generator(seed, base, distribution, base_offset_years, spread_months):
    """
    Random date generator

    Args:
        seed: random number generator's seed.
        base: base date
        distribution: logistic | laplace | gumbel | normal
        base_offset_years: base location offset of the distribution
        spread_months: spread of the distribution

    Returns:
        yields a random date
    """
    rng = np.random.default_rng(seed)
    base_date = np.datetime64(base, 'D')
    method = getattr(rng, distribution)
    loc_days = base_offset_years * 365.25
    scale_days = spread_months * 30.4375
    while True:
        r = -1
        while r < 0:
            r = int(method(loc_days, scale_days))
        random_date = base_date - np.timedelta64(r, 'D')
        yield random_date


def gender_generator(seed, male_female_ratio):
    """
    Random gender generator

    Args:
        seed: random gender generator's seed.
        male_female_ratio: 0.4 -> 40 males to 60 females

    Returns:
        yields a random gender
    """
    rng = np.random.default_rng(seed)
    while True:
        r = rng.integers(0, 100)
        gender = "female" if r >= 100 * male_female_ratio else "male"
        yield gender


def phone_number_generator(seed, csv_filename) -> Generator[(str, str), str, None]:
    """
    Random phone number generator

    Args:
        seed: random phone number generator's seed.
        csv_filename: file

    Returns:
        yields a random phone number
    """
    rng = np.random.default_rng(seed)
    lookup_table = pd.read_csv(csv_filename, header=None)
    lookup_table.columns = ['city', 'code']
    y = None
    while True:
        town = yield y
        code = lookup_table.loc[lookup_table['city'] == town]
        if code.index.size != 0:
            c = code['code'].iloc[0]
        else:
            print("Not found: " + town)
            c = "099 999XXXX"
        phone_number = re.sub('X', lambda x: str(rng.integers(0, 8)), c)
        y = phone_number


def national_id_generator(seed) -> Generator[str, (str, str), None]:
    """
    Random national id generator

    Args:
        seed: random national_id generator's seed.

    Returns:
        yields a national id
    """
    dob_map = {}
    rng = np.random.default_rng(seed)
    y = None
    while True:
        meta = yield y
        dob = meta[0].replace('-', '')
        count = dob_map.get(dob, None)
        count = count + 1 if count is not None else 5001
        dob_map[dob] = count
        gender = '0' if meta[1] == 'female' else '1'
        r = rng.integers(10, 100)
        y = dob + str(count) + gender + r.astype(str)
