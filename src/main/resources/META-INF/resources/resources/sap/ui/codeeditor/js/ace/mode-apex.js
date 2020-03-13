ace.define("ace/mode/doc_comment_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"],function(r,e,m){"use strict";var o=r("../lib/oop");var T=r("./text_highlight_rules").TextHighlightRules;var D=function(){this.$rules={"start":[{token:"comment.doc.tag",regex:"@[\\w\\d_]+"},D.getTagRule(),{defaultToken:"comment.doc",caseInsensitive:true}]};};o.inherits(D,T);D.getTagRule=function(s){return{token:"comment.doc.tag.storage.type",regex:"\\b(?:TODO|FIXME|XXX|HACK)\\b"};};D.getStartRule=function(s){return{token:"comment.doc",regex:"\\/\\*(?=\\*)",next:s};};D.getEndRule=function(s){return{token:"comment.doc",regex:"\\*\\/",next:s};};e.DocCommentHighlightRules=D;});ace.define("ace/mode/apex_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules","ace/mode/doc_comment_highlight_rules"],function(r,e,m){"use strict";var o=r("../lib/oop");var T=r("../mode/text_highlight_rules").TextHighlightRules;var D=r("../mode/doc_comment_highlight_rules").DocCommentHighlightRules;var A=function(){var a=this.createKeywordMapper({"variable.language":"activate|any|autonomous|begin|bigdecimal|byte|cast|char|collect|const"+"|end|exit|export|float|goto|group|having|hint|import|inner|into|join|loop|number|object|of|outer"+"|parallel|pragma|retrieve|returning|search|short|stat|synchronized|then|this_month"+"|transaction|type|when","keyword":"private|protected|public|native|synchronized|abstract|threadsafe|transient|static|final"+"|and|array|as|asc|break|bulk|by|catch|class|commit|continue|convertcurrency"+"|delete|desc|do|else|enum|extends|false|final|finally|for|from|future|global"+"|if|implements|in|insert|instanceof|interface|last_90_days|last_month"+"|last_n_days|last_week|like|limit|list|map|merge|new|next_90_days|next_month|next_n_days"+"|next_week|not|null|nulls|on|or|override|package|return"+"|rollback|savepoint|select|set|sort|super|testmethod|this|this_week|throw|today"+"|tolabel|tomorrow|trigger|true|try|undelete|update|upsert|using|virtual|webservice"+"|where|while|yesterday|switch|case|default","storage.type":"def|boolean|byte|char|short|int|float|pblob|date|datetime|decimal|double|id|integer|long|string|time|void|blob|Object","constant.language":"true|false|null|after|before|count|excludes|first|includes|last|order|sharing|with","support.function":"system|apex|label|apexpages|userinfo|schema"},"identifier",true);function k(v){if(v.slice(-3)=="__c")return"support.function";return a(v);}function s(b,d){return{regex:b+(d.multiline?"":"(?=.)"),token:"string.start",next:[{regex:d.escape,token:"character.escape"},{regex:d.error,token:"error.invalid"},{regex:b+(d.multiline?"":"|$"),token:"string.end",next:d.next||"start"},{defaultToken:"string"}]};}function c(){return[{token:"comment",regex:"\\/\\/(?=.)",next:[D.getTagRule(),{token:"comment",regex:"$|^",next:"start"},{defaultToken:"comment",caseInsensitive:true}]},D.getStartRule("doc-start"),{token:"comment",regex:/\/\*/,next:[D.getTagRule(),{token:"comment",regex:"\\*\\/",next:"start"},{defaultToken:"comment",caseInsensitive:true}]}];}this.$rules={start:[s("'",{escape:/\\[nb'"\\]/,error:/\\./,multiline:false}),c("c"),{type:"decoration",token:["meta.package.apex","keyword.other.package.apex","meta.package.apex","storage.modifier.package.apex","meta.package.apex","punctuation.terminator.apex"],regex:/^(\s*)(package)\b(?:(\s*)([^ ;$]+)(\s*)((?:;)?))?/},{regex:/@[a-zA-Z_$][a-zA-Z_$\d\u0080-\ufffe]*/,token:"constant.language"},{regex:/[a-zA-Z_$][a-zA-Z_$\d\u0080-\ufffe]*/,token:k},{regex:"`#%",token:"error.invalid"},{token:"constant.numeric",regex:/[+-]?\d+(?:(?:\.\d*)?(?:[LlDdEe][+-]?\d+)?)\b|\.\d+[LlDdEe]/},{token:"keyword.operator",regex:/--|\+\+|===|==|=|!=|!==|<=|>=|<<=|>>=|>>>=|<>|<|>|!|&&|\|\||\?\:|[!$%&*+\-~\/^]=?/,next:"start"},{token:"punctuation.operator",regex:/[?:,;.]/,next:"start"},{token:"paren.lparen",regex:/[\[]/,next:"maybe_soql",merge:false},{token:"paren.lparen",regex:/[\[({]/,next:"start",merge:false},{token:"paren.rparen",regex:/[\])}]/,merge:false}],maybe_soql:[{regex:/\s+/,token:"text"},{regex:/(SELECT|FIND)\b/,token:"keyword",caseInsensitive:true,next:"soql"},{regex:"",token:"none",next:"start"}],soql:[{regex:"(:?ASC|BY|CATEGORY|CUBE|DATA|DESC|END|FIND|FIRST|FOR|FROM|GROUP|HAVING|IN|LAST"+"|LIMIT|NETWORK|NULLS|OFFSET|ORDER|REFERENCE|RETURNING|ROLLUP|SCOPE|SELECT"+"|SNIPPET|TRACKING|TYPEOF|UPDATE|USING|VIEW|VIEWSTAT|WHERE|WITH|AND|OR)\\b",token:"keyword",caseInsensitive:true},{regex:"(:?target_length|toLabel|convertCurrency|count|Contact|Account|User|FIELDS)\\b",token:"support.function",caseInsensitive:true},{token:"paren.rparen",regex:/[\]]/,next:"start",merge:false},s("'",{escape:/\\[nb'"\\]/,error:/\\./,multiline:false,next:"soql"}),s('"',{escape:/\\[nb'"\\]/,error:/\\./,multiline:false,next:"soql"}),{regex:/\\./,token:"character.escape"},{regex:/[\?\&\|\!\{\}\[\]\(\)\^\~\*\:\"\'\+\-\,\.=\\\/]/,token:"keyword.operator"}],"log-start":[{token:"timestamp.invisible",regex:/^[\d:.() ]+\|/,next:"log-header"},{token:"timestamp.invisible",regex:/^  (Number of|Maximum)[^:]*:/,next:"log-comment"},{token:"invisible",regex:/^Execute Anonymous:/,next:"log-comment"},{defaultToken:"text"}],"log-comment":[{token:"log-comment",regex:/.*$/,next:"log-start"}],"log-header":[{token:"timestamp.invisible",regex:/((USER_DEBUG|\[\d+\]|DEBUG)\|)+/},{token:"keyword",regex:"(?:EXECUTION_FINISHED|EXECUTION_STARTED|CODE_UNIT_STARTED"+"|CUMULATIVE_LIMIT_USAGE|LIMIT_USAGE_FOR_NS"+"|CUMULATIVE_LIMIT_USAGE_END|CODE_UNIT_FINISHED)"},{regex:"",next:"log-start"}]};this.embedRules(D,"doc-",[D.getEndRule("start")]);this.normalizeRules();};o.inherits(A,T);e.ApexHighlightRules=A;});ace.define("ace/mode/folding/cstyle",["require","exports","module","ace/lib/oop","ace/range","ace/mode/folding/fold_mode"],function(r,e,a){"use strict";var o=r("../../lib/oop");var R=r("../../range").Range;var B=r("./fold_mode").FoldMode;var F=e.FoldMode=function(c){if(c){this.foldingStartMarker=new RegExp(this.foldingStartMarker.source.replace(/\|[^|]*?$/,"|"+c.start));this.foldingStopMarker=new RegExp(this.foldingStopMarker.source.replace(/\|[^|]*?$/,"|"+c.end));}};o.inherits(F,B);(function(){this.foldingStartMarker=/([\{\[\(])[^\}\]\)]*$|^\s*(\/\*)/;this.foldingStopMarker=/^[^\[\{\(]*([\}\]\)])|^[\s\*]*(\*\/)/;this.singleLineBlockCommentRe=/^\s*(\/\*).*\*\/\s*$/;this.tripleStarBlockCommentRe=/^\s*(\/\*\*\*).*\*\/\s*$/;this.startRegionRe=/^\s*(\/\*|\/\/)#?region\b/;this._getFoldWidgetBase=this.getFoldWidget;this.getFoldWidget=function(s,f,b){var l=s.getLine(b);if(this.singleLineBlockCommentRe.test(l)){if(!this.startRegionRe.test(l)&&!this.tripleStarBlockCommentRe.test(l))return"";}var c=this._getFoldWidgetBase(s,f,b);if(!c&&this.startRegionRe.test(l))return"start";return c;};this.getFoldWidgetRange=function(s,f,b,c){var l=s.getLine(b);if(this.startRegionRe.test(l))return this.getCommentRegionBlock(s,l,b);var m=l.match(this.foldingStartMarker);if(m){var i=m.index;if(m[1])return this.openingBracketBlock(s,m[1],b,i);var d=s.getCommentFoldRange(b,i+m[0].length,1);if(d&&!d.isMultiLine()){if(c){d=this.getSectionRange(s,b);}else if(f!="all")d=null;}return d;}if(f==="markbegin")return;var m=l.match(this.foldingStopMarker);if(m){var i=m.index+m[0].length;if(m[1])return this.closingBracketBlock(s,m[1],b,i);return s.getCommentFoldRange(b,i,-1);}};this.getSectionRange=function(s,b){var l=s.getLine(b);var c=l.search(/\S/);var d=b;var f=l.length;b=b+1;var g=b;var m=s.getLength();while(++b<m){l=s.getLine(b);var i=l.search(/\S/);if(i===-1)continue;if(c>i)break;var h=this.getFoldWidgetRange(s,"all",b);if(h){if(h.start.row<=d){break;}else if(h.isMultiLine()){b=h.end.row;}else if(c==i){break;}}g=b;}return new R(d,f,g,s.getLine(g).length);};this.getCommentRegionBlock=function(s,l,b){var c=l.search(/\s*$/);var d=s.getLength();var f=b;var g=/^\s*(?:\/\*|\/\/|--)#?(end)?region\b/;var h=1;while(++b<d){l=s.getLine(b);var m=g.exec(l);if(!m)continue;if(m[1])h--;else h++;if(!h)break;}var i=b;if(i>f){return new R(f,c,i,l.length);}};}).call(F.prototype);});ace.define("ace/mode/apex",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/apex_highlight_rules","ace/mode/folding/cstyle","ace/mode/behaviour/cstyle"],function(r,e,m){"use strict";var o=r("../lib/oop");var T=r("../mode/text").Mode;var A=r("./apex_highlight_rules").ApexHighlightRules;var F=r("../mode/folding/cstyle").FoldMode;var C=r("../mode/behaviour/cstyle").CstyleBehaviour;function a(){T.call(this);this.HighlightRules=A;this.foldingRules=new F();this.$behaviour=new C();}o.inherits(a,T);a.prototype.lineCommentStart="//";a.prototype.blockComment={start:"/*",end:"*/"};e.Mode=a;});(function(){ace.require(["ace/mode/apex"],function(m){if(typeof module=="object"&&typeof exports=="object"&&module){module.exports=m;}});})();
