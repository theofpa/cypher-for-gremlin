/*
 * Copyright (c) 2018-2019 "Neo4j, Inc." [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.gremlin.translation.walker

import org.opencypher.gremlin.translation.GremlinSteps
import org.opencypher.gremlin.translation.context.WalkerContext
import org.opencypher.gremlin.translation.walker.NodeUtils._
import org.opencypher.v9_0.ast._
import org.opencypher.v9_0.expressions._
import org.opencypher.v9_0.util.InputPosition.NONE

/**
  * AST walker that handles translation
  * of the `MERGE` clause nodes in the Cypher AST.
  */
object MergeWalker {
  def walkClause[T, P](context: WalkerContext[T, P], g: GremlinSteps[T, P], node: Merge): Unit = {
    new MergeWalker(context, g).walkClause(node)
  }
}

private class MergeWalker[T, P](context: WalkerContext[T, P], g: GremlinSteps[T, P]) {

  def walkClause(node: Merge): Unit = {
    val Merge(Pattern(patternParts), actions: Seq[MergeAction], _) = node
    walkMerge(g, patternParts, actions)
  }

  private def walkMerge(g: GremlinSteps[T, P], patternParts: Seq[PatternPart], actions: Seq[MergeAction]): Unit = {
    ensureFirstStatement(g, context)

    val createG = g.start().identity()
    val contextCreateG = context.copy()
    CreateWalker.walkClause(contextCreateG, createG, Create(Pattern(patternParts)(NONE))(NONE))

    val matchG = g.start()
    MatchWalker.walkPatternParts(context, matchG, patternParts, None)

    actions.foreach {
      case OnMatch(action: SetClause)  => SetWalker.walkClause(context, matchG, action)
      case OnCreate(action: SetClause) => SetWalker.walkClause(contextCreateG, createG, action)
    }

    g.choose(matchG, matchG, createG)
  }
}
