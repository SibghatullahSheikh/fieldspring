///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 Jason Baldridge, The University of Texas at Austin
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
package opennlp.fieldspring.tr.util;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Class for keeping constant values.
 *
 * @author  Jason Baldridge
 * @version $Revision: 1.53 $, $Date: 2006/10/12 21:20:44 $
 */
public class Constants {

    /**
     * Machine epsilon for comparing equality in floating point numbers.
     */
    public static final double EPSILON = 1e-6;

    // the location of Fieldspring
    public final static String FIELDSPRING_DIR = System.getenv("FIELDSPRING_DIR");
    public final static String FIELDSPRING_DATA = "data";

    // the location of the OpenNLP models
    public final static String OPENNLP_MODELS = Constants.getOpenNLPModelsDir();


    public static String getOpenNLPModelsDir() {
      String dir = System.getenv("OPENNLP_MODELS");
      if (dir == null) {
        dir = System.getProperty("opennlp.models");
        if (dir == null) {
          dir = FIELDSPRING_DIR + File.separator + "data/models";
          //dir = System.getProperty("user.dir") + File.separator + "data/models";
        }
      }
      return dir;
    }

    public static String getGazetteersDir() {
      String dir = (System.getenv("FIELDSPRING_DATA")!=null)? System.getenv("FIELDSPRING_DATA"):FIELDSPRING_DATA + File.separator + "gazetteers";
      if (dir == null) {
        dir = System.getProperty("gazetteers");
        if (dir == null) {
          dir = FIELDSPRING_DIR + File.separator + "data/gazetteers";
          //dir = System.getProperty("user.dir") + File.separator + "data/gazetteers";
        }
      }
      return dir;
    }

    // the location of the World Gazetteer database file
//    public final static String WGDB_PATH = System.getenv("WGDB_PATH");

    // the location of the TR-CoNLL database file
//    public static final String TRDB_PATH = System.getenv("TRDB_PATH");

    // the location of the user's home directory
    public final static String USER_HOME = System.getProperty("user.home");

    // the current working directory
    public final static String CWD = System.getProperty("user.dir");

    // The format for printing precision, recall and f-scores.
    public static final DecimalFormat PERCENT_FORMAT = 
	new DecimalFormat("#,##0.00%");


}
