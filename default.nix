with import <nixpkgs> {};

stdenv.mkDerivation {
  name = "jjson";

  buildInputs = with pkgs; [
    openjdk8
  ];

  shellHook = ''
    export JAVA_HOME="${pkgs.openjdk8}";
  '';
}
