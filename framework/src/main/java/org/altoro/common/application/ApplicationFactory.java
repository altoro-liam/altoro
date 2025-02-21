/*
 * java-tron is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-tron is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.altoro.common.application;

import org.springframework.context.ApplicationContext;

public class ApplicationFactory {

  /**
   * Build a new cli application.
   */
  public static Application create(ApplicationContext ctx) {
    return ctx.getBean(ApplicationImpl.class);
  }

  /**
   * Build a new application.
   */
  public Application createApplication() {
    return new ApplicationImpl();
  }
}
