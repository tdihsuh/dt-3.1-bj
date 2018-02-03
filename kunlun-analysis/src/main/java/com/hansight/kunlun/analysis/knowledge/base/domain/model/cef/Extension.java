/**
 * Extension.java    2011-09-06
 *
 * Copyright 2011, Adam Lesperance
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */



package com.hansight.kunlun.analysis.knowledge.base.domain.model.cef;

//~--- non-JDK imports --------------------------------------------------------

import com.hansight.kunlun.analysis.knowledge.base.domain.model.cef.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


//~--- classes ----------------------------------------------------------------

/**
 * Object that holds the mapping of all the elements that are part of the CEF extension.
 * <p>
 * This object is immutable and once created no changes can be made!
 *
 * @version 1.0, 2011-09-06
 * @author Adam Lesperance
 */
public class Extension implements Serializable {

    /**
     * Logger object
     */
    private static final Logger LOG = LoggerFactory.getLogger( Extension.class );

    /** Serial version */
    private static final long serialVersionUID = 1L;

    //~--- fields -------------------------------------------------------------

    /** Holds the computed string output of the CEF Extension object */
    private final String asString;

    /** Holds the field mapping */
    private final Map<String, String> fields;

    /** Holds the computed hashCode for the CEF Extension object */
    private final int hashCode;


    //~--- constructors -------------------------------------------------------

    /**
     * Create a new extension object using the provided map. All of the key/value pairs are checked
     * to ensure they are valid and the map is made read-only.
     *
     * @param extensionFields
     *            the mapping of extension keys and their values
     * @throws InvalidExtensionKey
     *             if one of the provided keys is invalid
     */
    public Extension( final Map<String, String> extensionFields ) throws InvalidExtensionKey {

        // Use the # of pairs * 20 as an initial best-guess for the builder size
        final StringBuilder sb    = new StringBuilder( extensionFields.size() * 20 );
        Boolean             first = true;

        /*
         * Loop over all of the element pairs and add them to the string builder. Conveniently when
         * we escape the keys/values we also check to make sure they're valid and if they aren't an
         * exception will be thrown. So we not only ensure everything is okay at creation, but we
         * also pre-calculate the string variable so future calls are instant for the small price of
         * memory space.
         */
        for (final Entry<String, String> entry : extensionFields.entrySet()) {
            if (first) {
                first = false;
            }
            else {
                sb.append( " " );
            }


            sb.append( StringUtils.escapeExtensionKey( entry.getKey() ) );
            sb.append( "=" );
            sb.append( StringUtils.escapeExtensionValue( entry.getValue() ) );
        }


        // Should be changeable but cast it anyway
        fields = Collections.unmodifiableMap( extensionFields );

        Extension.LOG.debug( "The extension's string was calculated as {}", sb.toString() );

        asString = sb.toString();
        hashCode = fields.hashCode();
    }


    //~--- methods ------------------------------------------------------------

    @Override
    public boolean equals( final Object obj ) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        else if (!fields.equals( ((Extension) obj).getFields() )) {
            return false;
        }


        return true;
    }


    @Override
    public int hashCode() {
        return hashCode;
    }


    @Override
    public String toString() {
        return asString;
    }


    //~--- get methods --------------------------------------------------------

    /**
     * @return a copy of the fields present in the extension
     */
    public Map<String, String> getFields() {
        return new HashMap<String, String>( fields );
    }
}
