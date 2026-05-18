# Copyright 2026 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""A simple Python script for testing purposes.

This script prints the command line arguments, attempting to parse them as JSON.
"""

import json
from absl import app


def main(argv):
  print("Python skill received:")
  for arg in argv[1:]:
    try:
      print(json.loads(arg))
    except json.JSONDecodeError:
      print(arg)


if __name__ == "__main__":
  app.run(main)
