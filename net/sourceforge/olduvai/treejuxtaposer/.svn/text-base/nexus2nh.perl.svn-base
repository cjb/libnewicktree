#!/usr/bin/perl

# input: NEXUS format file (presumably containing a TREE block)
# generates: 1 or more New Hampshire/Newick format files in current directory
#  
# adapted from the nexus2lv.perl converter to H3 format files
#
# Tamara Munzner Fri Sep  7 14:27:17 2001

$stage = 0;
$level = 0;
$comment = 0;
$translation = 0;

#$line = <STDIN>;
#$line =~ /^#[nN][eE][xX][uU][sS](.*)/ || die "Not a Nexus file\n";

while (<STDIN>) {
  chop;
  $line = $_;
  while (length($line) > 0) {  
    if (0 == $stage) {
      $line =~ /^#[nN][eE][xX][uU][sS](.*)/ || die "Not a Nexus file\n";
      $line = $1;
      $stage = 1;
    } elsif ($stage > 0 && $line =~ /^\[([^\]]*)(.*)/) {
      $line = $2;
      $comment++;
    } elsif ($stage > 0 && $comment > 0 && $line =~ /^\](.*)/) {
      $comment--;
      $line = $1;
    } elsif ($comment > 0) {
      $line = "";
    } elsif (1 == $stage && $line =~ /[bB][eE][gG][iI][nN] [tT][rR][eE][eE][sS]\s*;?\s*(.*)/) {
      $stage = 2;
      $line = $1;
    } elsif (2 == $stage && $line =~ /\s*[tT][rR][aA][nN][sS][lL][aA][tT][eE]\s*(.*)/) {
      $stage = 4;
      $line = $1;
    } elsif (2 == $stage && $line =~ /[tT][rR][eE][eE]\s*([^=]*)\s*=\s*[^\(]\s*(.*)/) {
      # the [&R] or [&U] after the equal sign is optional, I guess...
      $treename = $1;
      $line = $2;
      chop($treename);
      open(TREE, ">$treename.nh");
      $stage = 3;
    } elsif (3 == $stage) {
      if ($line =~ /^\s*\((.*)/) {
	print TREE "(";
	$line = $1;
	$level++;
      } elsif ($line =~ /^\s*,(.*)/) {
	$currname = $names[$currname] if 1 == $translation;
	print TREE "$currname,";
	undef($currname);
	$line = $1;
      } elsif ($line =~ /^\s*\)(.*)/) {
	$currname = $names[$currname] if 1 == $translation;
	print TREE "$currname)";
	undef($currname);
	$level--;
	$line = $1;
      } elsif ($line =~ /^\s*$/) {
	$line = "";
      } elsif ($line =~ /^\s*;(.*)/ && 0 == $level) {
	print TREE ";\n";
	close TREE;
	$line = $1;
	$stage = 2;
	$innernode = 0;
      } elsif ($line =~ /^\s*([^\(\),\n\[]*)(.*)/) {
	($currname,$length) = split(':',$1);
	$line = $2;
      }
    } elsif (4 == $stage) {
      if ($line =~ /\s*(\S*)\s+([^,;]*)\s*,?(.*)/) {
	$translation = 1;
	$num = $1;
	$name = $2;
	$line = $3;
	if (length($name) > 0) {
	    if (length($found{$name}) > 0) {
		# only add the number on the end if there's a need to
		# disambiguate two identical names
		$names[$num] = $name . "_" . $num;
		$oldnum = $found{$name};
		$names[$oldnum] = $name . "_" . $oldnum;
		print STDERR "Disambiguated name: $name\n";
	    } else {
		$names[$num] = $name;
	    }
	}
	$found{$name} = $num;	   
      } elsif ($line =~ /\s*;(.*)/) {
	$line = $1;
	$stage = 2;
      }
    } else {
      $line = "";
    }
   }
}



