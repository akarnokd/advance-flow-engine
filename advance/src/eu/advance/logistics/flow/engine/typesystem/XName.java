/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package eu.advance.logistics.flow.engine.typesystem;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.inference.TypeRelation;

/**
 * The XML naming record.
 *  @author akarnokd
 */
public class XName implements XComparable<XName> {
	/** The name. */
	public String name;
	/** The associated semantics. */
	public XSemantics semantics;
	/** The other aliases for this capability under the given semantics. */
	public Set<String> aliases;
	@Override
	public TypeRelation compareTo(XName o) {
		if (name.equals(o.name) 
				|| (o.aliases != null && o.aliases.contains(name)) 
				|| (aliases != null && aliases.contains(o.name))) {
			if (semantics != null && o.semantics != null) {
				return semantics.compareTo(o.semantics);
			}
			return TypeRelation.EQUAL;
		}
		
		return TypeRelation.NONE;
	}
	/**
	 * Assign values from another instance.
	 * @param other the another instance
	 */
	public void assign(@NonNull XName other) {
		this.name = other.name;
		this.semantics = other.semantics;
		this.aliases = other.aliases != null ? Sets.newHashSet(other.aliases) : null;
	}
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("XName { name = ").append(name);
		if (semantics != null) {
			b.append(", semantics = ").append(semantics);
		}
		if (aliases != null && !aliases.isEmpty()) {
			b.append(", aliases = ").append(aliases);
		}
		b.append(" }");
		return b.toString();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XName) {
			XName other = (XName) obj;
			return (Objects.equal(name, other.name) && Objects.equal(semantics, other.semantics));
		}
		return false;
	}
	@Override
	public int hashCode() {
		return Objects.hashCode(name, semantics);
	}
}
