///////////////////////////////////////////////////////////////////////////////
//  Copyright (C) 2010 Travis Brown, The University of Texas at Austin
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
///////////////////////////////////////////////////////////////////////////////
package opennlp.fieldspring.tr.text.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import opennlp.fieldspring.tr.text.Document;
import opennlp.fieldspring.tr.text.DocumentSource;
import opennlp.fieldspring.tr.text.Token;

public abstract class TextSource extends DocumentSource {
  protected final BufferedReader reader;

  public TextSource(BufferedReader reader) throws IOException {
    this.reader = reader;
  }

  protected String readLine() {
    String line = null;
    try {
      line = this.reader.readLine();
    } catch (IOException e) {
      System.err.println("Error while reading document source.");
    }
    return line;
  }

  public void close() {
    try {
      this.reader.close();
    } catch (IOException e) {
      System.err.println("Error while closing document source.");
    }
  }
}

