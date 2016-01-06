/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * <h1>Validation</h1>
 * Validation is separated into groups in order to allow validation before id
 * generation (where all fields used for id generation need to be validated) and
 * entity validation before saving. Due to the fact that often entities only
 * use a small subset of properties id generation validation group is set to the
 * non-default group (see <a href="https://docs.oracle.com/javaee/6/tutorial/doc/gkagv.html">Grouping Constraints of the The Java EE 6 Tutorial</a> for details) {@link IdValidation}.
 */
package richtercloud.document.scanner.model;
